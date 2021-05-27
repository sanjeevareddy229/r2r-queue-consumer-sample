package org.mcgill.ca.it.main;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * This class to find the environment and configure RabbitMQ and the IBM API connect from the java application.
 */
public class RabbitMQConfiguration {
    private String queueName;
    private String apiUrl;
    private String usernameRB;
    private String passwordRB;
    private String apiUsername;
    private String apiPassword;
    private String apiFolpalUrl;
    private String env;
    private String configFilePath;
    private Map<String, Object> configuration;
    private int timeout;

    private RabbitMQConfiguration() {
    }

    public RabbitMQConfiguration(String usernameRB, String passwordRB, String apiUsername, String apiPassword, String apiFolpalUrl, String queueName, String apiUrl, String configFilePath, String env) {
        this.queueName = queueName;
        this.apiUrl = apiUrl;
        this.usernameRB = usernameRB;
        this.passwordRB = passwordRB;
        this.apiUsername = apiUsername;
        this.apiPassword = apiPassword;
        this.apiFolpalUrl = apiFolpalUrl;
        this.configFilePath = configFilePath;
        this.env = env;

        ObjectMapper mapper = new ObjectMapper();
        try {
            configuration = mapper.readValue(new FileInputStream(this.getConfigFilePath()), new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            configuration = null;
            System.err.println(e.getMessage());
        }

        switch (env) {
            case "D":
                this.setApiUrl(configuration.get("devibmurl").toString());
                this.setApiFolpalUrl(configuration.get("fdevibmurl").toString());
                break;
            case "I":
                this.setApiUrl(configuration.get("intibmurl").toString());
                this.setApiFolpalUrl(configuration.get("fintibmurl").toString());
                break;
            case "Q":
                this.setApiUrl(configuration.get("qaibmurl").toString());
                this.setApiFolpalUrl(configuration.get("fqaibmurl").toString());
                break;
            case "P":
                this.setApiUrl(configuration.get("productionibmurl").toString());
                this.setApiFolpalUrl(configuration.get("fproductionibmurl").toString());
                break;
            default:
                System.err.println("Unable to read configuration");
        }

        try {
            this.timeout = Integer.parseInt(configuration.get("timeout").toString());
        } catch (NullPointerException npe) {
            System.out.println("#\tNo timeout configuration set. Default will be 300 seconds.");
            this.timeout = 300;
        }
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getUsernameRB() {
        return usernameRB;
    }

    public void setUsernameRB(String usernameRB) {
        this.usernameRB = usernameRB;
    }

    public String getPasswordRB() {
        return passwordRB;
    }

    public void setPasswordRB(String passwordRB) {
        this.passwordRB = passwordRB;
    }

    public String getApiUsername() {
        return apiUsername;
    }

    public void setApiUsername(String apiUsername) {
        this.apiUsername = apiUsername;
    }

    public String getApiPassword() {
        return apiPassword;
    }

    public void setApiPassword(String apiPassword) {
        this.apiPassword = apiPassword;
    }

    public String getApiFolpalUrl() {
        return apiFolpalUrl;
    }

    public void setApiFolpalUrl(String apiFolpalUrl) {
        this.apiFolpalUrl = apiFolpalUrl;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getConfigFilePath() {
        return configFilePath;
    }

    public void setConfigFilePath(String configFilePath) {
        this.configFilePath = configFilePath;
    }

    public Map<String, Object> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, Object> configuration) {
        this.configuration = configuration;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public Channel connectRabbitMQ()
            throws KeyManagementException, NoSuchAlgorithmException, IOException {

        ConnectionFactory factory = new ConnectionFactory();
        
        //System.out.println("*******RabbitMQ Credentials*********");
        //System.out.println("The rabbitmq username :"+this.getUsernameRB());
        //System.out.println("the rabbitmq password :"+this.getPasswordRB());
        //System.out.println();
        
        factory.setUsername(this.getUsernameRB());
        factory.setPassword(this.getPasswordRB());
        
       // System.out.println("Setting portnumber :"+Integer.parseInt(this.getConfiguration().get("portNum").toString()));
 
        factory.setPort(Integer.parseInt(this.getConfiguration().get("portNum").toString()));
        factory.useSslProtocol();
System.out.println(Integer.parseInt(this.getConfiguration().get("portNum").toString()));
        switch (this.getEnv()) {
            case "D":
            	
            //	System.out.println(this.getConfiguration().get("d-vhost").toString()+"this is virtual host name ");
            //	System.out.println(this.getConfiguration().get("d-hostName").toString()+"This is host name ");
                
            	factory.setHost(this.getConfiguration().get("d-hostName").toString());
                factory.setVirtualHost(this.getConfiguration().get("d-vhost").toString());
         
                break;
            case "Q":
         /*       System.out.println("********hostnames ************");
            	System.out.println("hostname :"+this.getConfiguration().get("q-hostName").toString());
                System.out.println("virtual hostname :"+this.getConfiguration().get("q-vhost").toString());
                System.out.println("*********rabbima credentials end****");
         */   	factory.setHost(this.getConfiguration().get("q-hostName").toString());
                factory.setVirtualHost(this.getConfiguration().get("q-vhost").toString());
                break;
            case "I":
                factory.setHost(this.getConfiguration().get("int-hostName").toString());
                factory.setVirtualHost(this.getConfiguration().get("int-vhost").toString());
                break;
            case "P":
/*
                System.out.println("********hostnames ************");
            	System.out.println("hostname :"+this.getConfiguration().get("p-hostName").toString());
                System.out.println("virtual hostname :"+this.getConfiguration().get("p-vhost").toString());
                System.out.println("*********rabbima credentials end****");
*/                
            	factory.setVirtualHost(this.getConfiguration().get("p-vhost").toString());
                factory.setHost(this.getConfiguration().get("p-hostName").toString());
                break;
                
            default:
                System.out.println("Something went wrong no environment got selected");
        }

        //System.out.println("Setting timeout variable");
        factory.setConnectionTimeout(30000);
        
        // System.out.println("calling connection factory***");
        Connection connection = factory.newConnection();
        if(connection.equals(connection)){
        	System.out.println("connection object returns ");
        }else{
        	System.out.println("No connection object");
        }
        
        return connection.createChannel();
    }

	public String getExc_queue() {
		// TODO Auto-generated method stub
		return this.getConfiguration().get("R2R_GEN_exc_queue").toString();
	}
}
