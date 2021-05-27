
package org.mcgill.ca.it.main;

import com.rabbitmq.client.AMQP.Queue;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import org.json.JSONObject;
import org.mcgill.ca.it.log.LogFileGenerator;
import org.mcgill.ca.it.service.ApiConnectClient;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

public class Main {
	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, IOException,
			ShutdownSignalException, ConsumerCancelledException, InterruptedException {
		RabbitMQConfiguration configuration = new RabbitMQConfiguration(args[2], args[3], args[4], args[5], null,
				args[6], null, args[1], args[0]);

		
		
		LogFileGenerator files = new LogFileGenerator(args[7], args[9], args[8], args[10]);

		// System.out.println("Arguments are : "+args[0]+" "+args[2]+" "+args[3]+"
		// "+args[4]+" "+args[5]+" "+args[6]+" "+args[7]+" "+args[8]+" "+args[9]+"
		// "+args[10]);

		// System.out.println("Calling connect rabbitmq function ");

		Channel channel = configuration.connectRabbitMQ();

		// Channel channel_exec = configuration.connectRabbitMQ();

		// System.out.println("channel returned");
		// System.out.println("checking the message count in the queue");

		Queue.DeclareOk response = channel.queueDeclarePassive(configuration.getQueueName());
		int messageCount = response.getMessageCount();

		// System.out.println("The message count would be :" + messageCount);

		int messageConsumed = 0;
		int messageRejected = 0;
		int messageTransferred = 0;

		channel.queueDeclare(configuration.getQueueName(), true, false, false, null);
		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(configuration.getQueueName(), false, consumer);

		for (int k = 0; k < messageCount; k++) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();

			if (delivery != null) {

				try {

					String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
					boolean isHires = false;

					//System.out.println("The message is :" + message);

					JSONObject jsonMap = new JSONObject(message);

					if (jsonMap.getString("businessEvent").matches("(.*)sometestString(.*)")
							|| jsonMap.getString("businessEvent").matches("(.*)sometestString(.*)")) {
						isHires = true;

					}

					if (ApiConnectClient.connectIbmapi(message, configuration, files, isHires)) {
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
						messageConsumed++;
					} else {

						if (isHires) {
                            
					        channel.basicPublish("", configuration.getExc_queue(), null, message.getBytes());
					        messageTransferred++;
					        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
							
						} else {

							channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
							messageRejected++;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
				}
			}
		}
		files.reportFile(messageCount, messageConsumed, messageRejected, messageTransferred);
		System.exit(0);
	}
}
