package com.cesgroup.api.user;

import cn.hutool.core.thread.ThreadFactoryBuilder;

import java.net.InetAddress;
import java.util.concurrent.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 账户日志客户端
 * 
 * @author 国栋
 *
 */
public class AccountLogClient {

	private ThreadPoolExecutor asyncLogthreadPool;

    private String server;

    private String url;

    /**
     * 初始化
     */
    @PostConstruct
    public void init() {
        try {
            server = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


		asyncLogthreadPool = new ThreadPoolExecutor(
				2, 10,
				1, TimeUnit.HOURS,
				new ArrayBlockingQueue<>(100),
				new ThreadFactoryBuilder().setNamePrefix("accountLogClient-threadpool-%d").build(),
				new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @PreDestroy
    public void close() {
		asyncLogthreadPool.shutdown();
    }

    /**
     * 日志
     * 
     * @param application
     *            应用
     * @param result
     *            结果
     * @param reason
     *            原因
     * @param username
     *            用户名
     * @param client
     *            客户端
     * @param description
     *            描述
     */
    public void log(String application, String result, String reason, String username,
        String client, String description) {
        try {
            AccountLogWorker accountLogWorker = new AccountLogWorker();
            accountLogWorker.setUrl(url);
            accountLogWorker.setApplication(application);
            accountLogWorker.setUsername(username);
            accountLogWorker.setClient(client);
            accountLogWorker.setServer(server);
            accountLogWorker.setResult(result);
            accountLogWorker.setReason(reason);
            accountLogWorker.setDescription(description);
			asyncLogthreadPool.submit(accountLogWorker);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 日志
     * 
     * @param result
     *            结果
     * @param reason
     *            原因
     */
    public void log(String result, String reason) {
        try {
            AccountLogDTO accountLogDto = AccountLogHolder.getAccountLogDto();
            this.log(accountLogDto.getApplication(), result, reason, accountLogDto.getUsername(),
                accountLogDto.getClient(), accountLogDto.getDescription());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
