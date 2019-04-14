#!/usr/bin/env python3

import pika
import sys
import select
import time
from typing import Optional

import common
from common import errprint
from pika.adapters.select_connection import PollEvents


def maybe_read_stdin() -> Optional[str]:
    """Returns one line from stdin, if there is any. None otherwise"""

    # credit: https://stackoverflow.com/a/3763257
    if select.select([sys.stdin, ], [], [], 0.0)[0]:
        return sys.stdin.readline().strip()
    else:
        return None


def get_injury(line: str) -> dict:
    if not line:
        return None
    parts = line.split(maxsplit=1)

    if len(parts) != 2:
        print("Invalid input, discarding. Should be: <injury> <name>")
        return None

    return parts[0]


class Doctor:
    """Class handling the Doctor lifecycle.
    Handles rabbitmq connection and periodically polls stdin
    to check for new patients"""

    POLL_PERIOD = 0.1

    def __init__(self):
        self.connection = None
        self.channel = None
        self.queue_name = None

    def start(self):
        self.connection = pika.SelectConnection(
            pika.ConnectionParameters(host='localhost'), on_open_callback=self._on_connected)
        self.connection.ioloop.start()


    def _on_results(self, ch, method, properties, body):
        print(body.decode())


    def _check_stdin(self):
        skill = sys.argv[1]

        line = maybe_read_stdin()
        skill = get_injury(line)

        if skill:
            routing_key = 'request.' + skill
            message = line
            properties = pika.BasicProperties(reply_to=self.queue_name)

            self.channel.basic_publish(exchange=common.EXCHANGE,
                                       routing_key=routing_key,
                                       body=message,
                                       properties=properties)

        self.connection.ioloop.call_later(Doctor.POLL_PERIOD,
                                          self._check_stdin)

    def _on_connected(self, connection):
        errprint("Connection created")
        self.connection.channel(on_open_callback=self._on_channel_open)

    def _on_channel_open(self, channel):
        errprint("Channel opened")
        self.channel = channel
        self.channel.add_on_close_callback(self._on_channel_closed)
        self.channel.exchange_declare(exchange=common.EXCHANGE, exchange_type='topic',
                                      callback=self._on_exchange_declared)

        self.connection.ioloop.call_later(1, self._check_stdin)

    def _on_exchange_declared(self, frame):
        errprint("Exchange declared")
        self.channel.queue_declare('', callback=self._on_queue_declared)

    def _on_queue_declared(self, result):
        errprint("Queue for responses declared")
        self.queue_name = result.method.queue
        self.channel.queue_bind(exchange=common.EXCHANGE,
                                queue=self.queue_name, routing_key=self.queue_name,
                                callback=self._on_queue_bind)

    def _on_queue_bind(self, result):
        self.channel.basic_consume(queue=self.queue_name, auto_ack=True,
                                   on_message_callback=self._on_results)

    def _on_channel_closed(self, channel, reason):
        self.channel = None
        errprint("Channel closed because of ", reason)


if __name__ == '__main__':
    # TODO handle INFO

    d = Doctor()
    d.start()
