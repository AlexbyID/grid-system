package com.alexbyid.worker.service;

import com.alexbyid.worker.web.dto.request.TaskRequest;
import com.alexbyid.worker.utils.reflexion.ReflectionUtils;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.nio.file.Path;

@Service
@Setter
public class SolverService {

    private Method solveMethod = null;
    private Path matrixPath = null;


    public Object solve(TaskRequest taskDTO) {
        Object[] solveArgs = {matrixPath, taskDTO.getStart(), taskDTO.getCount()};
        return ReflectionUtils.executeMethod(solveMethod, solveArgs);
    }

}
