package com.example.Controller;

import com.example.Entity.HolidayRequest;
import com.example.Repo.HolidayRequestRepo;
import com.example.Entity.User;
import com.example.Repo.UserRepo;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;


@RestController
@RequestMapping(path="/api/HolidayRequests")
public class HolidayRequestController {

    public static class NotFoundException extends Exception{}
    public static class PayloadException extends Exception{}
    public static class NoSearchResultException extends Exception{}

    @Autowired
    private HolidayRequestRepo holidayRequestRepo;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private UserRepo userRepo;
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

    //Creates HolidayRequest, saves it to DB and starts new ProcessInstance of "Urlaubsantrag"
   @PostMapping(value = "/create", consumes ={"application/json"},produces = {"application/json"})
     String createNewHolidayRequest(@RequestBody() HolidayRequest newHolidayRequest) throws PayloadException{

       if (newHolidayRequest == null || newHolidayRequest.getEndDate() == null || newHolidayRequest.getStartDate() == null || newHolidayRequest.getFullName() == null || newHolidayRequest.getVorgesetzter() == null){
           throw new PayloadException();
       }

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
      return "Holidayrequest created!";
    }

    //Shows all HolidayRequests
    @GetMapping(path = "", produces = {"application/json"})
    List<HolidayRequest> allHolidayRequests() throws NoSearchResultException {
       List<HolidayRequest> allHolidayRequests = (List<HolidayRequest>) holidayRequestRepo.findAll();

       if (allHolidayRequests.isEmpty()){
           throw new NoSearchResultException();
       }

        return  allHolidayRequests;
    }

    //Show  specific Holidayrequest
    @GetMapping(path = "/{id}", produces = {"application/json"})
    HolidayRequest showSpecificHolidayRequest(@PathVariable("id") long id) throws NotFoundException {
       checkExistingHolidayRequest(id);
       holidayRequest =  holidayRequestRepo.findById(id).get();

       return holidayRequest;
    }

    //Assigning holidayrequests
    @PostMapping(value = "/{id}/assign", consumes ={"application/json"},produces = {"application/json"})
    String assignTask(@RequestBody() User newUser , @PathVariable("id") long id) throws PayloadException, NotFoundException {

       checkExistingHolidayRequest(id);

       if (newUser == null || newUser.getName() == null || newUser.getUserId() == 0){
           throw new PayloadException();
       }

       userRepo.findById(newUser.getUserId()).orElseThrow(() -> new PayloadException());

       taskid = null;
       userid = Long.toString(newUser.getUserId());
       allTasks = getAllTasksForSpecificRequest(id);

        if (allTasks == null){
            return "No tasks available";
        }else {
            for (Task task: allTasks) {
                taskid = task.getId();
            }
            taskService.setAssignee(taskid, userid);
            updateStatus(id, newUser.getName(), "Assigned");
            return "Task Assigned";
        }
    }

    //Approving holidayrequests
    @PostMapping(value = "/{id}/approve", consumes ={"application/json"},produces = {"application/json"})
    String approveRequest(@RequestBody() User newUser , @PathVariable("id") long id) throws PayloadException, NotFoundException {

        checkExistingHolidayRequest(id);

        if (newUser == null || newUser.getName() == null || newUser.getUserId() == 0){
            throw new PayloadException();
        }

        userRepo.findById(newUser.getUserId()).orElseThrow(() -> new PayloadException());

        taskAssigne = null;
        taskid = null;
        userid = Long.toString(newUser.getUserId());
        variables.clear();
        variables.put("approved", "true");
        allTasks = getAllTasksForSpecificRequest(id);

        if (allTasks == null){
            return "No tasks available";
        }else {
            for (Task task : allTasks) {
                taskAssigne = task.getAssignee();
                taskid = task.getId();
            }
            if (!(userid.equals(taskAssigne))){
                return "You are not assigned for this Task!";
            }else{
                taskService.complete(taskid, variables);
                updateStatus(id, newUser.getName(), "Approved");
                return "Task completed!";
            }
        }
    }

    //Rejecting holidayrequest
    @PostMapping(value = "/{id}/reject", consumes ={"application/json"},produces = {"application/json"})
    String rejectRequest(@RequestBody() User newUser , @PathVariable("id") long id) throws NotFoundException, PayloadException {

        checkExistingHolidayRequest(id);

        if (newUser == null || newUser.getName() == null || newUser.getUserId() == 0){
            throw new PayloadException();
        }

        userRepo.findById(newUser.getUserId()).orElseThrow(() -> new PayloadException());

       taskAssigne = null;
       taskid = null;
       userid = Long.toString(newUser.getUserId());
       variables.clear();
       variables.put("approved", "false");
       allTasks = getAllTasksForSpecificRequest(id);

        if (allTasks == null){
            return "No tasks available";
        }else {
            for (Task task: allTasks) {
                taskAssigne = task.getAssignee();
                taskid = task.getId();
            }
            if (!(userid.equals(taskAssigne))){
                return "You are not assigned for this Task!";
            }else{
                taskService.complete(taskid, variables);
                updateStatus(id,newUser.getName(), "Rejected");
                return "Task completed!";
            }
        }
    }

    //Check if Holidayrequest with ID exists
    void checkExistingHolidayRequest(long id) throws NotFoundException {
        holidayRequestRepo.findById(id).orElseThrow(() -> new NotFoundException());
    }


    //Query all Tasks for a specific holidayrequest
   List<Task> getAllTasksForSpecificRequest(long id){
        allTasks = taskService.createTaskQuery().processVariableValueEquals("request_id",id).list();

        if (allTasks.size() == 0){
            return null;
        } else {
            return allTasks;
        }
   }

   //Updates status of holidayrequest
   void updateStatus(long id, String name, String status){
       holidayRequest = holidayRequestRepo.findById(id).get();
       holidayRequest.setStatus(status + " by " + name);
       holidayRequestRepo.save(holidayRequest);
   }

    //Exceptionhandling
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NotFoundException.class)
    public void handleNotFound(){}

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PayloadException.class)
    public void handleBadPayload(){}

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ExceptionHandler(NoSearchResultException.class)
    public void handleNoSearchResult(){}

}

