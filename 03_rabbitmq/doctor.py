#!/usr/bin/env python3

import pika
import sys

import common

if __name__ == '__main__':
    # TODO read requests in a loop

    channel = common.get_channel()
    skill = sys.argv[1]

    channel.basic_publish(exchange=common.EXCHANGE, routing_key='request.{}'.format(skill),
                          body='sóme body: {}'.format(skill))
