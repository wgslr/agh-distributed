#include <stdio.h>

#include "main.h"


/*********************************************************************************
* Global variables
*********************************************************************************/

struct in_addr next_addr;

sem_t *msg_queue_sem;

sem_t *token_available_sem;

message *msg_queue[MAX_MSG_QUEUE];
unsigned msg_queue_len;


/*********************************************************************************
* Predeclarations
*********************************************************************************/

pthread_t spawn(void *(*func)(void *), void *args);

int open_tcp_socket(const short port);

void accept_connection(int socket, int epoll_fd);

message *read_message(int socket);

void handle_message(const message *msg);

int queue_message(message *msg);


void semsignal(sem_t *sem);

void semwait(sem_t *sem);

void dumpmem(const void *pointer, size_t len);

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
            free(msg);
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
    semsignal(token_available_sem);

    fprintf(stderr, "Accepted incoming connection\n");
}

message *read_message(int socket) {
    ssize_t bytes;
    message *buff = calloc(1, sizeof(message) + MAX_BODY_LEN);
    bytes = recv(socket, buff, sizeof(message), 0);
    OK(bytes, "Error receiving message header")

    if(bytes == 0) {
        fprintf(stderr, "Peer closed connection\n");
        shutdown(socket, SHUT_RDWR);
        close(socket);
        free(buff);
        return NULL;
    }

    if(buff->len > 0) {
        bytes = recv(socket, &buff->data, buff->len, 0);
        OK(bytes, "Error receiving message body")
        if(bytes < (ssize_t) buff->len) {
            dumpmem(buff->data, bytes);
            fprintf(stderr, "Received truncated message, ignoring\n");
            return NULL;
        }
    }

    return buff;
}


void handle_message(const message *msg) {
    printf("Received message:\n");
    dumpmem(msg, sizeof(message));

}


void send_message(int socket, msg_type type, void *data, size_t len) {
    message *msg = calloc(1, sizeof(message) + len);
    msg->type = type;
    msg->len = len;
    if(len > 0) {
        memcpy(msg->data, data, len);
    }
    OK(send(socket, msg, sizeof(message) + len, 0), "Error sending message");
    free(msg);
}


/*********************************************************************************
* Sender
*********************************************************************************/

void *tcp_sender(void *args) {
    while(true){
        fprintf(stderr, "Waiting for token\n");
        semwait(token_available_sem);
        semwait(msg_queue_sem);
        if(msg_queue_len > 0) {
            // @TODO send message
            printf("Sending message\n");
            --msg_queue_len;
        }

        semsignal(msg_queue_sem);
    }

}

// Adds message to the to-send queue
int queue_message(message *msg) {
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


/*********************************************************************************
* Entrypoint
*********************************************************************************/

int main(int argc, char *argv[]) {
    if(argc != 6) {
        fprintf(stderr, "Not enough CLI parameters\n");
        return 1;
    }

    const char *name = argv[1];
    const int listener_port = atoi(argv[2]);
    const char *succ = argv[3];
    const bool start_with_token = argv[4][0] == '1';
    const bool use_tcp = strcmp("tcp", argv[5]) == 0 ? true : false;

    msg_queue_sem = calloc(1, sizeof(sem_t));
    token_available_sem = calloc(1, sizeof(sem_t));

    sem_init(msg_queue_sem, 0, 1);
    sem_init(token_available_sem, 0, (unsigned int) start_with_token);


    spawn(&tcp_listener, (void *) &listener_port);
    spawn(&tcp_sender, NULL);

    message msg = {
            .type = EMPTY,
            .destination_name = "alamakota"
    };

    queue_message(&msg);
    queue_message(&msg);

    pause();


    return 0;
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

void dumpmem(const void *pointer, size_t len) {
    uint8_t *p = pointer;
    for(size_t i = 0; i < len; ++i) {
        printf("%02hX", *(p + i));
        if(i % 32 == 31)
            printf("\n");
        else if(i % 4 == 3)
            printf(" ");
    }
    printf("\n");
}

