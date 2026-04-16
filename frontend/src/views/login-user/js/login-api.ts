/**
 * 处理登录逻辑及数据缓存
 */
export const doLogin = async (username: string) => {
    console.log(`用户 ${username} 正在尝试登录...`);

    try {
        // 模拟 API 请求
        const responseData: any = await new Promise((resolve: (value: any) => void) => {
            setTimeout(() => {
                resolve({
                    code: 200,
                    data: {
                        token: "mock_token_123456",
                        userInfo: { name: username, role: "admin" }
                    }
                });
            }, 800);
        });

        if (responseData.code === 200) {
            // --- 缓存逻辑 ---
            const { token, userInfo } = responseData.data;

            // 存储 Token
            localStorage.setItem('token', token);
            // 存储用户信息（JS对象需要转成字符串）
            localStorage.setItem('user_info', JSON.stringify(userInfo));

            return { success: true, data: userInfo };
        }
        return { success: false };
    } catch (error) {
        console.error("登录异常", error);
        return { success: false };
    }
};
