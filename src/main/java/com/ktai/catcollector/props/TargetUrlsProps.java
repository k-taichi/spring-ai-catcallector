package com.ktai.catcollector.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gmogshd taichi-kaneko
 */
@Component
@Data
@ConfigurationProperties(prefix = "cat-collector")
public class TargetUrlsProps {
    List<TargetUrls> targetUrls;
}
