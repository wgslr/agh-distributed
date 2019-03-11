//
// Created by wojciech on 3/11/19.
//

#ifndef INC_01_SOCKETS_MAIN_H
#define INC_01_SOCKETS_MAIN_H

#include <arpa/inet.h>
#include <assert.h>
#include <errno.h>
#include <netinet/in.h>
#include <pthread.h>
#include <signal.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/epoll.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/un.h>
#include <unistd.h>


#define OK(_EXPR, _ERR_MSG) if((_EXPR) < 0) { fprintf(stderr, "%s: %d %s\n", _ERR_MSG, errno, strerror(errno)); exit(1); }
#define CHECK(_EXPR, _ERR_MSG) if((_EXPR) < 0) { fprintf(stderr, "%s: %d %s\n", _ERR_MSG, errno, strerror(errno)); }

#define TOKEN_DELAY 1000
#define NAME_LEN 64
#define BODY_LEN 1024



typedef enum {
    NEW_CLIENT = 0x11,
    WITH_PAYLOAD = 0x22,
    EMPTY = 0x33,
} msg_type;


typedef struct {
    msg_type type;
    uint32_t len;
    char source_name[NAME_LEN + 1];
    char destination_name[NAME_LEN + 1];
    uint32_t data[0];
} message;

#endif //INC_01_SOCKETS_MAIN_H
