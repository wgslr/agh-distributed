#!/usr/bin/env python3

import pika
import sys
import select
import time
from typing import Optional

from interactive_server import InteractiveServer
import common
from common import errprint


def get_injury(line: str) -> dict:
    if not line:
        return None
    parts = line.split(maxsplit=1)

    if len(parts) != 2:
        print("Invalid input, discarding. Should be: <injury> <name>")
        return None

    return parts[0]


class Doctor(InteractiveServer):
    def __init__(self):
        queues = [
            ('', self._on_result),
            ('info', self._on_info)
        ]
        super().__init__(self._handle_line, queues)

    def _handle_line(self, line):
        injury = get_injury(line)
        if injury:
            routing_key = 'request.' + injury
            message = line
            properties = pika.BasicProperties(reply_to=self.auto_queue)

            self.channel.basic_publish(exchange=common.EXCHANGE,
                                       routing_key=routing_key,
                                       body=message,
                                       properties=properties)

    def _on_result(self, ch, method, properties, body):
        print("result recevied: ", body.decode())

    def _on_info(self, ch, method, properties, body):
        print("[INFO] {}".format(body.decode()))


if __name__ == '__main__':
    doctor = Doctor()
    doctor.start()
