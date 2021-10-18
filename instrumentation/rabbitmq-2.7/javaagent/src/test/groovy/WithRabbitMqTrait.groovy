/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

import com.rabbitmq.client.ConnectionFactory
import org.testcontainers.containers.GenericContainer

import java.time.Duration

trait WithRabbitMqTrait {

  static GenericContainer rabbitMqContainer
  static ConnectionFactory connectionFactory

  def startRabbit() {
    rabbitMqContainer = new GenericContainer('rabbitmq:latest')
      .withExposedPorts(5672)
      .withStartupTimeout(Duration.ofSeconds(120))
    rabbitMqContainer.start()

    connectionFactory = new ConnectionFactory(
      host: rabbitMqContainer.containerIpAddress,
      port: rabbitMqContainer.getMappedPort(5672)
    )
  }

  def stopRabbit() {
    rabbitMqContainer?.stop()
  }
}