#!/usr/bin/env python3

"""Defines a class capable of managing RabbitMQ connection
and polling stdin for input"""

import pika
import sys
import select
import time
from typing import Optional, Callable, Tuple

import common
from common import errprint
from pika.channel import Channel


def maybe_read_stdin() -> Optional[str]:
    """Returns one line from stdin, if there is any. None otherwise"""

    # credit: https://stackoverflow.com/a/3763257
    if select.select([sys.stdin, ], [], [], 0.0)[0]:
        return sys.stdin.readline().strip()
    else:
        return None


class InteractiveServer:
    """Generic server capable of reading stdin.
    Uses asynchronous connection SelectConnection"""

    def __init__(self, stdin_handler: Callable, queues_spec: Tuple[str, Callable]):
        self.POLL_PERIOD = 0.1
        self.connection: pika.SelectConnection = None
        self.channel: Optional[Channel] = None
        self.queues_spec = queues_spec
        self.stdin_handler = stdin_handler

    def start(self):
        self.connection = pika.SelectConnection(
            pika.ConnectionParameters(host='localhost'), on_open_callback=self._on_connected)
        self.connection.ioloop.start()

    def _check_stdin(self):
        line = maybe_read_stdin()

        if line:
            self.stdin_handler(line)

        self.connection.ioloop.call_later(self.POLL_PERIOD, self._check_stdin)

    def _on_connected(self, connection):
        errprint("Connection created")
        self.connection.channel(on_open_callback=self._on_channel_open)

    def _on_channel_open(self, channel):
        errprint("Channel opened")
        self.channel = channel
        self.channel.add_on_close_callback(self._on_channel_closed)
        self.channel.exchange_declare(exchange=common.EXCHANGE, exchange_type='topic',
                                      callback=self._on_exchange_declared)

        self.connection.ioloop.call_later(self.POLL_PERIOD, self._check_stdin)

    def _on_exchange_declared(self, frame):
        errprint("Exchange declared")
        self._declare_queues()

    def _declare_queues(self):
        for key, callback in self.queues_spec:
            def on_declared(result, key=key, callback=callback):
                return self._on_queue_declared(result, key, callback)

            self.channel.queue_declare('',
                                       exclusive=True,
                                       callback=on_declared)

    def _on_queue_declared(self, result, routing_key, msg_callback):
        queue_name = result.method.queue
        if routing_key == '':
            routing_key = queue_name
            self.auto_queue = queue_name

        self.channel.queue_bind(exchange=common.EXCHANGE,
                                queue=queue_name,
                                routing_key=routing_key,
                                callback=lambda result: self._on_queue_bind(result, queue_name, msg_callback))

    def _on_queue_bind(self, _result, queue, msg_callback):
        self.channel.basic_consume(queue=queue, auto_ack=True,
                                   on_message_callback=msg_callback)

    def _on_channel_closed(self, channel, reason):
        self.channel = None
        errprint("Channel closed because of ", reason)
