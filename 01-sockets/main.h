//
// Created by wojciech on 3/11/19.
//

#ifndef INC_01_SOCKETS_MAIN_H
#define INC_01_SOCKETS_MAIN_H

#include <assert.h>
#include <errno.h>
#include <pthread.h>
#include <semaphore.h>
#include <signal.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/epoll.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <netinet/in.h>
#include <arpa/inet.h>

// for getline
#define __USE_GNU
#include <stdio.h>

#define OK(_EXPR, _ERR_MSG) if((_EXPR) < 0) { fprintf(stderr, "%s: %d %s\n", _ERR_MSG, errno, strerror(errno)); exit(1); }
#define CHECK(_EXPR, _ERR_MSG) if((_EXPR) < 0) { fprintf(stderr, "%s: %d %s\n", _ERR_MSG, errno, strerror(errno)); }

#define TOKEN_DELAY 1
#define MAX_NODE_NAME_LEN 64
#define MAX_BODY_LEN 1024
#define MAX_CONN_QUEUE 16
#define MAX_MSG_QUEUE 32



typedef enum {
    NEW_CLIENT = 0x11,
    WITH_PAYLOAD = 0x22,
    EMPTY = 0x33,
} msg_type;


typedef struct {
    msg_type type;
    uint32_t len;
    char source_name[MAX_NODE_NAME_LEN + 1];
    char destination_name[MAX_NODE_NAME_LEN + 1];
    uint32_t data[0];
} message;

#endif //INC_01_SOCKETS_MAIN_H
