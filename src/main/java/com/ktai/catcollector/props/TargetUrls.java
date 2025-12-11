package com.ktai.catcollector.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author gmogshd taichi-kaneko
 */
@Data
@Component
@ConfigurationProperties(prefix = "cat-collector.target-urls")
public class TargetUrls {
    String url;
    boolean crawled=false;

}
