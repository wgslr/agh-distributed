#!/usr/bin/env python3

import pika
import sys

import common
from common import errprint


def make_resp_queue(channel):
    queue_name = ''

    errprint("binding queue")
    result = channel.queue_declare(queue_name, exclusive=True)
    queue_name = result.method.queue
    print("obtained quee {}".format(queue_name))

    channel.queue_bind(exchange=common.EXCHANGE,
                       queue=queue_name, routing_key=queue_name)

    return queue_name


def on_results(ch, method, properties, body):
    print(body.decode())


if __name__ == '__main__':
    # TODO read requests in a loop

    connection, channel = common.connect()
    skill = sys.argv[1]

    respq = make_resp_queue(channel)

    channel.basic_publish(exchange=common.EXCHANGE, routing_key='request.{}'.format(skill),
                          body='some body: {}'.format(skill), properties=pika.BasicProperties(reply_to=respq))
    channel.basic_consume(queue=respq, auto_ack=True,
                          on_message_callback=on_results)

    while True:
        connection.process_data_events()
