package com.cesgroup.api.user;

/**
 * 账户日志操作
 * 
 * @author 国栋
 *
 */
public class AccountLogHolder {

    private static ThreadLocal<AccountLogDTO> threadLocal = new ThreadLocal<AccountLogDTO>();

    /**
     * 初始化
     * 
     * @param application
     *            应用
     * @param username
     *            用户名
     * @param clientIp
     *            客户端IP
     * @param description
     *            描述
     */
    public static void init(String application, String username, String clientIp,
        String description) {
        AccountLogDTO accountLogDto = new AccountLogDTO();
        accountLogDto.setApplication(application);
        accountLogDto.setUsername(username);
        accountLogDto.setClient(clientIp);
        accountLogDto.setDescription(description);
        threadLocal.set(accountLogDto);
    }

    public static AccountLogDTO getAccountLogDto() {
        return threadLocal.get();
    }

    public static void clear() {
        threadLocal.remove();
    }
}
