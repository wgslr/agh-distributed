// Wojciech Geisler
// 2019-03

#define _XOPEN_SOURCE
#define _POSIX_C_SOURCE 200809L
// inet_aton
#define _DEFAULT_SOURCE


#include <arpa/inet.h>
#include "main.h"


/*********************************************************************************
* Global variables
*********************************************************************************/

struct in_addr next_addr;

sem_t *msg_queue_sem;

sem_t *token_available_sem;

message *msg_queue[MAX_MSG_QUEUE];
unsigned msg_queue_len;

char *node_name;

struct sockaddr_in own_addr;
struct sockaddr_in successor_addr;

int log_fd = -1;

/*********************************************************************************
* Predeclarations
*********************************************************************************/

pthread_t spawn(void *(*func)(void *), void *args);

int open_tcp_socket(short port);

void accept_connection(int socket, int epoll_fd);

bool eq_addr(const struct sockaddr_in *addr1, const struct sockaddr_in *addr2);

message *read_message(int socket);

void handle_message(message *msg);

int push_message(message *msg);

void signal_token(void);

void send_hello(void);

void semsignal(sem_t *sem);

void semwait(sem_t *sem);

void dumpmem(const void *pointer, size_t len);

void dumpmsg(const message *msg);

struct sockaddr_in parse_address(char *addr_str);

void input_loop(void);

void log_token(void);

void send_log(const char *msg);

message *pop_message(void);

/*********************************************************************************
* Connection
*********************************************************************************/

void *tcp_listener(void *args) {
    const short *port = (short *) args;
    int server_socket = open_tcp_socket(*port);

    int epoll_fd = epoll_create1(0);
    struct epoll_event event = {
            .events = EPOLLIN | EPOLLPRI,
            .data.fd = server_socket
    };
    OK(epoll_ctl(epoll_fd, EPOLL_CTL_ADD, server_socket, &event), "Could not add local socket to epoll");

    fprintf(stdout, "Listening at 0.0.0.0:%d\n", *port);

    while(true) {
        OK(epoll_wait(epoll_fd, &event, 1, -1), "Error waiting for message");

        int event_socket = event.data.fd;
        if(event_socket == server_socket) {
            accept_connection(event_socket, epoll_fd);
        } else {
            message *msg = read_message(event_socket);
            if(msg != NULL) {
                handle_message(msg);
            }
        }
    }
}

int open_tcp_socket(const short port) {
    const struct sockaddr_in addr = {
            .sin_family = AF_INET,
            .sin_port =  htons(port),
            .sin_addr.s_addr = INADDR_ANY
    };

    const int fd = socket(AF_INET, SOCK_STREAM, 0);
    OK(fd, "Error opening inet socket");

    int val = 1;
    OK(setsockopt(fd, SOL_SOCKET, SO_REUSEADDR, &val, sizeof(val)), "Error setting socket opt");

    OK(bind(fd, (const struct sockaddr *) &addr, sizeof(addr)), "Error binding inet socket");
    OK(listen(fd, MAX_CONN_QUEUE), "Error listening on inet socket");

    return fd;
}

void accept_connection(int socket, int epoll_fd) {
    int client_fd = accept(socket, NULL, NULL);
    OK(client_fd, "Error accepting connection");

    struct epoll_event event = {
            .events = EPOLLIN,
            .data.fd = client_fd
    };
    OK(epoll_ctl(epoll_fd, EPOLL_CTL_ADD, client_fd, &event), "Error adding connection to epoll");

    fprintf(stderr, "Accepted incoming connection\n");
}

message *read_message(int socket) {
    ssize_t bytes;
    message *buff = calloc(1, sizeof(message) + MAX_BODY_LEN);
    bytes = recv(socket, buff, sizeof(message), 0);
    CHECK(bytes, "Error receiving message header")

    if(bytes == 0) {
        fprintf(stderr, "Peer closed connection\n");
        shutdown(socket, SHUT_RDWR);
        close(socket);
        free(buff);
        return NULL;
    }

    if(buff->len > 0) {
        bytes = recv(socket, &buff->data, buff->len, 0);
        CHECK(bytes, "Error receiving message body")
        if(bytes < (ssize_t) buff->len) {
            fprintf(stderr, "Received truncated message, ignoring\n");
            free(buff);
            return NULL;
        }
    }

    return buff;
}


void handle_message(message *msg) {
    fprintf(stderr, "Received message:\n");
//    msg->type != EMPTY ? dumpmsg(msg) : NULL;
    dumpmsg(msg);

    bool received_token = true;

    if(msg->type == WITH_PAYLOAD && strcmp(msg->destination_name, node_name) == 0) {
        fprintf(stdout, "Message from %s: %s", msg->source_name, (char *) msg->data);
        free(msg);
    } else if(strcmp(msg->source_name, node_name) == 0) {
        fprintf(stderr, "%s could not be reached, discarding message after full circle.\n", msg->destination_name);
        free(msg);
    } else if(msg->type == OOB_HELLO) {
        // message from an incoming client, insert into normal token-managed flow
        received_token = false;
        msg->type = HELLO;
        push_message(msg);
    } else if(msg->type == HELLO) {
        hello_body *body = (hello_body *) msg->data;
        if(eq_addr(&body->successor_addr, &successor_addr)) {
            // insert new client into the ring before our successor
            successor_addr = body->sender_addr;
            fprintf(stdout, "Changed successor to %s\n", msg->source_name);
            free(msg);
        } else {
            push_message(msg);
        }
    } else if(msg->type == WITH_PAYLOAD) {
        push_message(msg);
    } else {
        free(msg);
    }

    if(received_token) {
        signal_token();
        log_token();
    }
}


void signal_token(void) {
    // ensure the semaphore is binary for sanity
    while(sem_trywait(token_available_sem) == 0) {}
    semsignal(token_available_sem);
}


void log_token(void) {
    char msg[1024];
    sprintf(msg, "{\"node\":\"%s\", \"message\":\"received token\"}", node_name);
    send_log(msg);
}


/*********************************************************************************
* Sender
*********************************************************************************/

void *tcp_sender(void *args) {
    (void) args;
    struct sockaddr_in peer_addr = {0};

    int socket_fd = -1;
    while(true) {
        if(!eq_addr(&peer_addr, &successor_addr)) {
            shutdown(socket_fd, 0);
            socket_fd = socket(AF_INET, SOCK_STREAM, 0);
            OK(socket_fd, "Error creating sender socket");
            while(connect(socket_fd, (struct sockaddr *) &successor_addr, sizeof(successor_addr)) < 0) {
                fprintf(stderr, "Error connecting to peer socket: %d %s\n", errno, strerror(errno));
                usleep(100000);
            }
            peer_addr = successor_addr;
        }

        fprintf(stderr, "Waiting for token\n");
        semwait(token_available_sem);
        sleep(TOKEN_DELAY);

        message *to_send = pop_message();

        CHECK(send(socket_fd, to_send, sizeof(message) + to_send->len, 0), "Error sending message");
        free(to_send);
    }
}

bool eq_addr(const struct sockaddr_in *addr1, const struct sockaddr_in *addr2) {
    return addr1->sin_family == addr2->sin_family &&
           addr1->sin_port == addr2->sin_port &&
           addr1->sin_addr.s_addr == addr2->sin_addr.s_addr;
}

// Adds message to the to-send queue
int push_message(message *msg) {
    semwait(msg_queue_sem);
    int result = 0;

    if(msg_queue_len == MAX_MSG_QUEUE) {
        fprintf(stderr, "Message queue is full\n");
        result = 1;
    } else {
        msg_queue[msg_queue_len] = msg;
        ++msg_queue_len;
    }

    semsignal(msg_queue_sem);
    return result;
}

message *pop_message(void) {
    message *popped = NULL;
    if(msg_queue_len > 0) {
        semwait(msg_queue_sem);

        popped = msg_queue[0];
        for(unsigned i = 0; i < msg_queue_len - 1; ++i) {
            msg_queue[i] = msg_queue[i + 1];
        }
        msg_queue[msg_queue_len] = NULL;
        --msg_queue_len;

        semsignal(msg_queue_sem);
    } else {
        // generate message to pass the token
        popped = calloc(1, sizeof(message));
        popped->type = EMPTY;
        popped->len = 0;
    }
    return popped;
}


void send_log(const char *msg) {
    if(log_fd < 0) {
        log_fd = socket(AF_INET, SOCK_DGRAM, 0);
        OK(log_fd, "Error creating datagram socket");
    }

    struct sockaddr_in addr = {0};
    addr.sin_family = AF_INET;
    addr.sin_addr.s_addr = inet_addr(MULTICAST_ADDR);
    addr.sin_port = htons(MULTICAST_PORT);

    CHECK(sendto(log_fd, msg, strlen(msg), 0, (struct sockaddr *) &addr, sizeof(addr)),
          "Error sending log on multicast");
}


/*********************************************************************************
* Entrypoint
*********************************************************************************/

int main(int argc, char *argv[]) {
    if(argc != 6) {
        fprintf(stderr, "Not enough CLI parameters\n");
        return 1;
    }

    node_name = calloc(MAX_NODE_NAME_LEN + 1, sizeof(char));
    strncpy(node_name, argv[1], MAX_NODE_NAME_LEN);
    own_addr = parse_address(argv[2]);
    successor_addr = parse_address(argv[3]);
    const bool start_with_token = argv[4][0] == '1';
    const bool use_tcp = strcmp("tcp", argv[5]) == 0 ? true : false;
    (void) use_tcp;

    msg_queue_sem = calloc(1, sizeof(sem_t));
    token_available_sem = calloc(1, sizeof(sem_t));

    sem_init(msg_queue_sem, 0, 1);
    sem_init(token_available_sem, 0, (unsigned int) start_with_token);


    short listener_port = ntohs(own_addr.sin_port);
    spawn(&tcp_listener, (void *) &listener_port);
    spawn(&tcp_sender, NULL);

    send_hello();

    input_loop();

    return 0;
}


void send_hello(void) {
    int socket_fd = socket(AF_INET, SOCK_STREAM, 0);
    OK(socket_fd, "Error creating hello socket");


    while(connect(socket_fd, (struct sockaddr *) &successor_addr, sizeof(successor_addr)) < 0) {
        fprintf(stderr, "Error connecting to peer socket: %d %s\n", errno, strerror(errno));
        usleep(500000);
    }
    message *msg = calloc(1, sizeof(message) + sizeof(hello_body));
    msg->len = sizeof(hello_body);
    msg->type = OOB_HELLO;
    strcpy(msg->source_name, node_name);
    strcpy(msg->destination_name, BROADCAST_NAME);

    hello_body *body = (hello_body *) msg->data;
    body->sender_addr = own_addr;
    body->successor_addr = successor_addr;

    OK(send(socket_fd, msg, sizeof(message) + msg->len, 0), "Error sending hello message");
    shutdown(socket_fd, 2);
}


void input_loop(void) {
    char *line = NULL;
    size_t n = 0;
    while(true) {

        OK(getline(&line, &n, stdin), "Error reading line from stdin");
        char *delim = strchr(line, ' ');
        if(delim == NULL) {
            printf("You must provide destination and content for the message\n");
            continue;
        }

        *delim = '\0';
        ++delim;
        message *msg = calloc(1, sizeof(message) + strlen(delim));

        msg->type = WITH_PAYLOAD;
        msg->len = (uint32_t) strlen(delim);
        strncpy(msg->destination_name, line, MAX_NODE_NAME_LEN);
        strncpy(msg->source_name, node_name, MAX_NODE_NAME_LEN);
        memcpy(msg->data, delim, strlen(delim));
        push_message(msg);
    }

}



/*********************************************************************************
* Utils
*********************************************************************************/

// Spawns a thread with default attributes
pthread_t spawn(void *(*func)(void *), void *args) {
    pthread_attr_t *attr = calloc(1, sizeof(pthread_attr_t));
    pthread_attr_init(attr);
    pthread_t tid;

    OK(pthread_create(&tid, attr, func, args), "Error creating network handler thread");
    pthread_attr_destroy(attr);
    free(attr);
    return tid;
}

void semsignal(sem_t *sem) {
    OK(sem_post(sem), "Semaphore post failed");
}

void semwait(sem_t *sem) {
    OK(sem_wait(sem), "Waiting for semaphore failed");
}


struct sockaddr_in parse_address(char *addr_str) {
    char *delim = strstr(addr_str, ":");
    *delim = '\0';
    const unsigned short port = (unsigned short) atoi(delim + 1);

    struct in_addr addr;
    inet_aton(addr_str, &addr);

    struct sockaddr_in parsed = {
            .sin_family = AF_INET,
            .sin_addr = addr,
            .sin_port =  htons(port),
    };
    return parsed;
}


void dumpmsg(const message *msg) {
    fprintf(stderr, "from: %s\nto: %s\nlen: %u\ndata: ", msg->source_name, msg->destination_name, msg->len);
    dumpmem(msg->data, msg->len);
}


void dumpmem(const void *pointer, const size_t len) {
    const uint8_t *p = pointer;
    for(size_t i = 0; i < len; ++i) {
        fprintf(stderr, "%02hX", *(p + i));
        if(i % 32 == 31)
            fprintf(stderr, "\n");
        else if(i % 4 == 3)
            fprintf(stderr, " ");
    }
    fprintf(stderr, "\n");
}

