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
package cn.smallbun.screw.boot;

import cn.smallbun.screw.boot.config.Screw;
import cn.smallbun.screw.boot.properties.ScrewProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Screw 文档生成工具自动配置类
 *
 * @author lengleng
 * @date 2021/11/20
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ScrewProperties.class)
public class ScrewAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Screw.class)
    public Screw screw(ScrewProperties screwProperties) {
        return new Screw(screwProperties);
    }
}
