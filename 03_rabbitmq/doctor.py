#!/usr/bin/env python3

import pika
import sys
import select
import time

import common
from common import errprint
from pika.adapters.select_connection import PollEvents


class Doctor:
    def __init__(self):
        self.connection = None
        self.channel = None

    def start(self):
        self.connection = pika.SelectConnection(
            pika.ConnectionParameters(host='localhost'), on_open_callback=self._on_connected)

        # self.connection.ioloop.call_later(3, self._on_later)
        self.connection.ioloop.start()

    def _on_later(self):
        # print("Am later")
        skill = sys.argv[1]
        print("checking stdin")


        if select.select([sys.stdin,],[],[],0.0)[0]:
            print("Have data!")
            content = sys.stdin.readline().strip()
            try:
                self.channel.basic_publish(exchange=common.EXCHANGE, routing_key='request.{}'.format(skill),
                                        body=content + ': {}'.format(skill), properties=pika.BasicProperties(reply_to=self.queue_name))
                #    body='some body: {}'.format(skill), properties=pika.BasicProperties())
            except Exception as err:
                print("Err ", err)
            else:
                print("sent")
        else:
            print("No data")
            # print(lines)
        self.connection.ioloop.call_later(1, self._on_later)

    def _on_connected(self, connection):
        errprint("connected")
        self.connection.channel(on_open_callback=self._on_channel_open)

    def _on_channel_open(self, channel):
        errprint("channel open")
        self.channel = channel
        self.channel.add_on_close_callback(self._on_channel_closed)
        self.channel.exchange_declare(exchange=common.EXCHANGE, exchange_type='topic',
                                      callback=self._on_exchange_declared)

        self.connection.ioloop.call_later(1, self._on_later)

        errprint("channel open2")

    def _on_exchange_declared(self, frame):
        errprint("exchange declared")
        self.channel.queue_declare('', callback=self._on_queue_declared)
        errprint("exchange declared2")

    def _on_queue_declared(self, result):
        errprint("queue declared")
        self.queue_name = result.method.queue
        self.channel.queue_bind(exchange=common.EXCHANGE,
                                queue=self.queue_name, routing_key=self.queue_name,
                                callback=self._on_queue_bind)

    def _on_queue_bind(self, result):
        errprint("queue bound")
        self.channel.basic_consume(queue=self.queue_name, auto_ack=True,
                                   on_message_callback=on_results)

    def _on_channel_closed(self, channel, reason):
        self.channel = None
        errprint("Channel closed because of ", reason)


def on_results(ch, method, properties, body):
    print(body.decode())


def on_stdin(fd, events):
    print("stdin happened")


if __name__ == '__main__':
    # TODO read requests in a loop

    d = Doctor()
    d.start()
    # try:
    #     connection.ioloop.add_handler(
    #         sys.stdin, on_stdin, PollEvents.READ | PollEvents.WRITE)
    #     # Loop so we can communicate with RabbitMQ
    #     connection.ioloop.start()
    # except KeyboardInterrupt:
    #     # Gracefully close the connection
    #     connection.close()
    #     # Loop until we're fully closed, will stop on its own
    #     connection.ioloop.start()
