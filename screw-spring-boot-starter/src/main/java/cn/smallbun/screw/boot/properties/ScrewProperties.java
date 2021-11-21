/*
 * screw-spring-boot-starter - screw-spring-boot-starter
 * Copyright © 2020 SanLi (qinggang.zuo@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cn.smallbun.screw.boot.properties;

import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.engine.EngineTemplateType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * screw 配置文件
 *
 * @author lengleng
 * @date 2021/11/20
 */
@Data
@ConfigurationProperties(prefix = "screw")
public class ScrewProperties {

    //====================基本配置====================//
    /**
     * 组织
     */
    private String             organization;
    /**
     * url
     */
    private String             organizationUrl;
    /**
     * 标题
     */
    private String             title;
    /**
     * 版本
     */
    private String             version;
    /**
     * 描述
     */
    private String             description;

    //====================连接配置====================//
    /**
     * 用户名
     */
    private volatile String    username;
    /**
     * 密码
     */
    private volatile String    password;

    /**
     * 驱动类名称
     */
    private String             driverClassName;
    /**
     * JDBC URL
     */
    private String             jdbcUrl;

    //====================数据处理配置====================//
    /**
     * 忽略表名
     */
    private List<String>       ignoreTableName;
    /**
     * 忽略表前缀
     */
    private List<String>       ignoreTablePrefix;
    /**
     * 忽略表后缀
     */
    private List<String>       ignoreTableSuffix;
    /**
     * 指定生成表名
     *
     * @see 1.0.3
     */
    private List<String>       designatedTableName;
    /**
     * 指定生成表前缀
     *
     * @see 1.0.3
     */
    private List<String>       designatedTablePrefix;
    /**
     * 指定生成表后缀
     *
     * @see 1.0.3
     */
    private List<String>       designatedTableSuffix;

    //====================生成引擎配置====================//
    /**
     * 生成文件类型
     */
    private EngineFileType     fileType    = EngineFileType.HTML;
    /**
     * 生成实现
     */
    private EngineTemplateType produceType = EngineTemplateType.freemarker;
    /**
     * 自定义模板，模板需要和文件类型和使用模板的语法进行编写和处理，否则将会生成错误
     */
    private String             template;

    /**
     * 文件名称
     */
    private String             fileName;
}
