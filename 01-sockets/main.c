#include <stdio.h>

#include "main.h"

/*********************************************************************************
* Predeclarations
*********************************************************************************/

pthread_t spawn(void *(*func)(void *), void *args);



/*********************************************************************************
* Connection
*********************************************************************************/

void* tcp_server(void *args) {
    while(true) {
        printf("I'm being a server!");
        sleep(TOKEN_DELAY);
    }

}


/*********************************************************************************
* Entrypoint
*********************************************************************************/

int main(int argc, char* argv[]) {
    if (argc < 6) {
        fprintf(stderr, "Not enough CLI parameters\n");
    }

    const char *name = argv[1];
    const int listener_port = atoi(argv[2]);
    const char *succ = argv[3];
    const bool start_with_token = (const bool) atoi(argv[4]);
    const bool use_tcp = strcmp("tcp", argv[5]) == 0 ? true : false;

    spawn(&tcp_server, 0);

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
