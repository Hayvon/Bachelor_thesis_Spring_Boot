package com.example;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.gson.JsonObject;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.task.Task;
import org.h2.util.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.NotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path="/api")
public class HolidayRequestController {

    @Autowired
    private HolidayRequestRepo holidayRequestRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private TaskService taskService;

    String taskid = null;
    String taskAssigne = null;
    String userid = null;
    List<Task> allTasks = null;
    HashMap<String, Object> variables = new HashMap<>();
    HolidayRequest holidayRequest = null;

    @GetMapping("/index.html")
    String indexpage(){
        return "index";
    }

    //Creates HolidayRequest, saves it to DB and starts new ProcessInstance of "Urlaubsantrag"
   @PostMapping(value = "/HolidayRequest/create", consumes ={"application/json"},produces = {"application/json"})
     void createNewHolidayRequest(@RequestBody() HolidayRequest newHolidayRequest){
        newHolidayRequest.setStatus("Pending. Waiting for Interaction");

        holidayRequestRepo.save(newHolidayRequest);

      runtimeService.createProcessInstanceByKey("urlaubsantrag")
              .setVariable("request_id", newHolidayRequest.getId())
              .setVariable("fullName", newHolidayRequest.getFullName())
              .setVariable("vorgesetzter", newHolidayRequest.getVorgesetzter())
              .setVariable("startDate", newHolidayRequest.getStartDate())
              .setVariable("endDate", newHolidayRequest.getEndDate())
              .setVariable("status", newHolidayRequest.getStatus())
              .executeWithVariablesInReturn();
    }

    //Shows all HolidayRequests
    @GetMapping(path = "/HolidayRequests", produces = {"application/json"})
    List<HolidayRequest> allHolidayRequests(){
        return (List<HolidayRequest>) holidayRequestRepo.findAll();
    }

    //Shows all Users   //TODO: Komische Verteilung der IDs
    @GetMapping(path = "/Users", produces = {"application/json"})
    List<User> allUsers(){
        return (List<User>) userRepo.findAll();
    }

    //Show Tasks for specific Holidayrequest
    @GetMapping(path = "/HolidayRequest/{id}", produces = {"application/json"})
    String showHolidayRequestTask(@PathVariable("id") long id){
        String taskString = "Keine Tasks zu diesem Urlaubsantrag verfügbar!";

        List<Task> allTasks = taskService.createTaskQuery().processVariableValueEquals("request_id",id).list();

        for (Task task: allTasks) {
            taskString = "Task: " + task.getName() + " ID: " + task.getId() + " Assignee: " + task.getAssignee();
        }
        return taskString;
    }

    //Creating user
    @PostMapping(value = "/User/create", consumes ={"application/json"},produces = {"application/json"})
    String createUser(@RequestBody() User user){
        userRepo.save(user);
        return "User created";
    }

    //Assigning holidayrequests
    @PostMapping(value = "/HolidayRequest/{id}/assign", consumes ={"application/json"},produces = {"application/json"}) //TODO: User anlegen speerat
    String claimTask(@RequestBody() User user , @PathVariable("id") long id){
        taskid = null;
        userid = Long.toString(user.getUserId());  //TODO: Nach dem Update des Status springt die Id um 1 hoch
        allTasks = getAllTasksForSpecificRequest(id);

        for (Task task: allTasks) {
           taskid = task.getId();
        }

        taskService.setAssignee(taskid, userid);
        updateStatus(id, user.getName(), "Assigned");
        return "Task Assigned";
    }

    //Approving holidayrequests
    @PostMapping(value = "/HolidayRequest/{id}/approve", consumes ={"application/json"},produces = {"application/json"})
    String approveRequest(@RequestBody() User user , @PathVariable("id") long id){
        taskAssigne = null;
        taskid = null;
        userid = Long.toString(user.getUserId());
        variables.put("approved", "true");
        allTasks = getAllTasksForSpecificRequest(id);

        for (Task task: allTasks) {
            taskAssigne = task.getAssignee();
            taskid = task.getId();
        }
       if (!(userid.equals(taskAssigne))){
           return "You are not assigned for this Task!";
       }else{
           taskService.complete(taskid, variables);
           updateStatus(id, user.getName(), "Approved");
           return "Task completed!";
       }
    }

    //Rejecting holidayrequest
    @PostMapping(value = "/HolidayRequest/{id}/reject", consumes ={"application/json"},produces = {"application/json"})
    String rejectRequest(@RequestBody() User user , @PathVariable("id") long id){
        String taskAssigne = null;
        String taskid = null;
        String userid = Long.toString(user.getUserId());
        variables.put("approved", "false");
        allTasks = getAllTasksForSpecificRequest(id);

        for (Task task: allTasks) {
            taskAssigne = task.getAssignee();
            taskid = task.getId();
        }
        if (!(userid.equals(taskAssigne))){
            return "You are not assigned for this Task!";
        }else{
            taskService.complete(taskid, variables);
            updateStatus(id,user.getName(), "Rejected");
            return "Task completed!";
        }
    }

    //Shows all active Tasks
    @GetMapping(path = "/tasks", produces = {"application/json"})
    String findAllTasks(){
        String taskString = null;
        List<Task> taskList = processEngine.getTaskService().createTaskQuery().active().list();
        for (Task task: taskList) {
          taskString = "Name: " + task.getName() + " ID: " + task.getId() + " Assignee: " + task.getAssignee();
        }
        return taskString;
    }


    //Query all Tasks for a specific holidayrequest
   List<Task> getAllTasksForSpecificRequest(long id){
        return taskService.createTaskQuery().processVariableValueEquals("request_id",id).list();
   }


   //Updates status of holidayrequest
   void updateStatus(long id, String name, String status){
       holidayRequest = holidayRequestRepo.findById(id).get();
       holidayRequest.setStatus(status + " by " + name);
       holidayRequestRepo.save(holidayRequest);
   }

}

