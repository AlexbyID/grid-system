package com.alexbyid.worker.system.state;

import com.alexbyid.worker.enums.WorkerStatusEnum;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class WorkerState {

    private WorkerStatusEnum workerStatusEnum = WorkerStatusEnum.UNINITIALIZED;

}
