CC=gcc
O=-O0
override CFLAGS+=-lm -lpthread -lrt -Wall -Wextra -Wall -std=c11 \
	-Wformat=2 -g \
	-Wswitch-default -Wswitch-enum -Wcast-align -Wpointer-arith \
	-Wbad-function-cast -Wstrict-overflow=5 -Wstrict-prototypes -Winline \
	-Wundef -Wnested-externs -Wcast-qual -Wshadow  \
	-Wlogical-op -Wfloat-equal -Wredundant-decls \
	-Wold-style-definition -Wuninitialized -Winit-self \
	-fdiagnostics-color=always \
	$(O)
SANITIZER=-fsanitize=address -fsanitize=undefined -fsanitize=leak -lasan -lubsan
.PHONY: all test clean debug

all: main

main: main.c *.h
	$(CC) $< -o $@ $(CFLAGS) $(SANITIZER)

