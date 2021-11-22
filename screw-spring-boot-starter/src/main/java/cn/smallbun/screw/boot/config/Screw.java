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
package cn.smallbun.screw.boot.config;

import cn.smallbun.screw.boot.properties.ScrewProperties;
import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.execute.DocumentationExecute;
import cn.smallbun.screw.core.process.ProcessConfig;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;

/**
 * @author lengleng
 * @date 2021/11/20
 * <p>
 * screw 文档输出
 */
@RequiredArgsConstructor
public class Screw {

    private final ScrewProperties screwProperties;

    /**
     * 生成文档 （文件形式）
     *
     * @param dataSource 目标数据源信息
     * @return
     */
    public ByteArrayOutputStream documentGeneration(DataSource dataSource) {
        return this.documentGeneration(dataSource, screwProperties);
    }

    /**
     * 生成文档 （文件形式）
     *
     * @param dataSource      目标数据源信息
     * @param screwProperties 配置信息
     * @return
     */
    public ByteArrayOutputStream documentGeneration(DataSource dataSource, ScrewProperties screwProperties) {
        //总配置
        cn.smallbun.screw.core.Configuration config = Configuration.builder()
                //组织
                .organization(screwProperties.getOrganization())
                //url
                .organizationUrl(screwProperties.getOrganizationUrl())
                //标题
                .title(screwProperties.getTitle())
                //版本
                .version(screwProperties.getVersion())
                //描述
                .description(screwProperties.getDescription())
                //数据源
                .dataSource(dataSource)
                //引擎模板配置
                .engineConfig(getEngineConfig())
                //数据处理配置
                .produceConfig(getProcessConfig()).build();
        //生成文档
        return new DocumentationExecute(config).execute();
    }


    /**
     * 引擎模板配置
     *
     * @return {@link EngineConfig}
     */
    private EngineConfig getEngineConfig() {
        return EngineConfig.builder()
                //文件类型
                .fileType(screwProperties.getFileType())
                //生成模板实现
                .produceType(screwProperties.getProduceType())
                //自定义模板位置
                .customTemplate(screwProperties.getTemplate())
                //文件名称
                .fileName(screwProperties.getFileName()).build();
    }

    /**
     * 数据处理配置
     *
     * @return {@link ProcessConfig}
     */
    private ProcessConfig getProcessConfig() {
        return ProcessConfig.builder()
                //忽略表名
                .ignoreTableName(screwProperties.getIgnoreTableName())
                //忽略表前缀
                .ignoreTablePrefix(screwProperties.getIgnoreTablePrefix())
                //忽略表后缀
                .ignoreTableSuffix(screwProperties.getIgnoreTableSuffix())
                //指定生成表名
                .designatedTableName(screwProperties.getDesignatedTableName())
                //指定生成表前缀
                .designatedTablePrefix(screwProperties.getDesignatedTablePrefix())
                //指定生成表后缀
                .designatedTableSuffix(screwProperties.getDesignatedTableSuffix()).build();
    }

}
