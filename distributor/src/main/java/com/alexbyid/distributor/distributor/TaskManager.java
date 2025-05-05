package com.alexbyid.distributor.distributor;

import com.alexbyid.distributor.system.dto.TaskDTO;
import com.alexbyid.distributor.system.dto.WorkerDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class TaskManager {
    @Value("${task-manager.subtask-size}")
    private int subtaskSize;
    @Value("${task-manager.subtasks-count}")
    private int subtasksCount;
    @Autowired
    private String callbackUrl;
    private List<TaskDTO> tasks = new ArrayList<>();
    private BigInteger start = BigInteger.ZERO;
    @Getter
    @Setter
    private BigInteger finalEnd;

    public TaskDTO getTask() {
        Optional<TaskDTO> task = tasks.stream().filter(taskDTO -> taskDTO.getWorker() == null).findFirst();
        if (task.isPresent()) return task.get();
        if (start.equals(finalEnd)) return null;

        generateTasks();
        return getTask();
    }

    private void generateTasks() {
        log.info("Generating new tasks");

        for (int i = 0; i < subtasksCount; i++) {

            if (start.compareTo(finalEnd) >= 0) {
                log.info("Reached finalEnd {}, stopping generation", finalEnd);
                break;
            }

            BigInteger end = start.add(BigInteger.valueOf(subtaskSize));
            if (end.compareTo(finalEnd) > 0) {
                end = finalEnd;
            }

            tasks.add(new TaskDTO(
                    start.toString(),
                    end.toString(),
                    callbackUrl
            ));

            start = end;

            if (start.compareTo(finalEnd) >= 0) {
                log.info("Final end {} reached, stopping generation", finalEnd);
                break;
            }
        }
    }

    public void removeCompetedTask(TaskDTO taskDTO) {
        log.info("Task completed {}:{}", taskDTO.getStart(), taskDTO.getCount());
        tasks.remove(taskDTO);
    }

    public void taskRedistribution(List<WorkerDTO> worker) {
        if (tasks.isEmpty()) return;

        List<String> workersAddresses = worker.stream()
                .map(WorkerDTO::fullAddress).toList();

        for (TaskDTO task : tasks.stream().filter(taskDTO -> taskDTO.getWorker() != null).toList()) {
            if (workersAddresses.contains(task.getWorker())) continue;
            log.warn("Worker {} abandoned his task", task.getWorker());
            task.setWorker(null);
        }
    }

    public void reset() {
        start = BigInteger.ZERO;
        tasks = new ArrayList<>();
    }

}
