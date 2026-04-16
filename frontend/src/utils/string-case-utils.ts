/**
 * 字符串大小写转换工具类
 */

/**
 * 使用实例：
 *
 * import { StringCaseUtils } from '@/utils/string-case-utils';
 *
 * const testString = "hello_world from-TypeScript";
 *
 * console.log(StringCaseUtils.toLowerCase(testString));
 * // 输出: "hello_world from-typescript"
 *
 * console.log(StringCaseUtils.toUpperCase(testString));
 * // 输出: "HELLO_WORLD FROM-TYPESCRIPT"
 *
 * console.log(StringCaseUtils.toCamelCase(testString));
 * // 输出: "helloWorldFromTypescript"
 *
 * console.log(StringCaseUtils.toPascalCase(testString));
 * // 输出: "HelloWorldFromTypescript"
 *
 * console.log(StringCaseUtils.toSnakeCase(testString));
 * // 输出: "hello_world_from_type_script"
 *
 * console.log(StringCaseUtils.toKebabCase(testString));
 * // 输出: "hello-world-from-type-script"
 */
export class StringCaseUtils {

    /**
     * 核心辅助方法：将字符串拆分成单词数组
     * 支持处理包含空格、下划线、中划线以及驼峰命名的字符串
     */
    private static getWords(str: string): string[] {
        if (!str) return [];

        return str
            // 在小写字母和大写字母之间插入空格 (例如: "camelCase" -> "camel Case")
            .replace(/([a-z])([A-Z])/g, '$1 $2')
            // 将所有非字母和非数字的字符替换为空格 (例如: "snake_case", "kebab-case" -> "snake case")
            .replace(/[^a-zA-Z0-9]+/g, ' ')
            // 去除首尾空格
            .trim()
            // 按空格拆分为数组
            .split(/\s+/);
    }

    /**
     * 1. 转换为全部小写
     * @example "HELLO WORLD" -> "hello world"
     */
    public static toLowerCase(str: string): string {
        return str.toLowerCase();
    }

    /**
     * 2. 转换为全部大写
     * @example "hello world" -> "HELLO WORLD"
     */
    public static toUpperCase(str: string): string {
        return str.toUpperCase();
    }

    /**
     * 3. 转换为小驼峰命名 (camelCase)
     * 首个单词全小写，后续单词首字母大写
     * @example "hello_world" -> "helloWorld"
     */
    public static toCamelCase(str: string): string {
        const words = this.getWords(str);
        return words.map((word, index) => {
            if (index === 0) return word.toLowerCase();
            return word.charAt(0).toUpperCase() + word.slice(1).toLowerCase();
        }).join('');
    }

    /**
     * 4. 转换为大驼峰/帕斯卡命名 (PascalCase)
     * 所有单词首字母大写
     * @example "hello_world" -> "HelloWorld"
     */
    public static toPascalCase(str: string): string {
        const words = this.getWords(str);
        return words.map(word =>
            word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
        ).join('');
    }

    /**
     * 5. 转换为蛇形命名 (snake_case)
     * 所有字母小写，单词间用下划线连接
     * @example "helloWorld" -> "hello_world"
     */
    public static toSnakeCase(str: string): string {
        const words = this.getWords(str);
        return words.map(word => word.toLowerCase()).join('_');
    }

    /**
     * 6. 转换为烤肉串命名 (kebab-case)
     * 所有字母小写，单词间用中划线连接
     * @example "helloWorld" -> "hello-world"
     */
    public static toKebabCase(str: string): string {
        const words = this.getWords(str);
        return words.map(word => word.toLowerCase()).join('-');
    }
}
