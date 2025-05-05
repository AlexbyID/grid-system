package com.alexbyid.distributor.service;

import com.alexbyid.distributor.utils.reflection.ReflectionUtils;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

@Service
@Setter
public class DistributorJarService {
    private Method processResult;

    public Object executeCalculateEnd(Method method, String dataJson) {
        Object[] calculateArgs = {dataJson};
        return ReflectionUtils.executeMethod(method, calculateArgs);
    }

    public Object executeProcessResult(String jsonResult) {
        Object[] processArgs = {jsonResult};
        return ReflectionUtils.executeMethod(processResult, processArgs);
    }

}
