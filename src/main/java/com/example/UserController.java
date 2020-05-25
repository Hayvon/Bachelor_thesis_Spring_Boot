package com.example;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path="/api/Users")
public class UserController {

    public static class NotFoundException extends Exception{}
    public static class PayloadException extends Exception{}
    public static class NoSearchResultException extends Exception{}

    @Autowired
    private UserRepo userRepo;

    //Shows all Users
    @GetMapping(path = "", produces = {"application/json"})
    List<User> allUsers() throws NoSearchResultException {
        List<User> userList = (List<User>) userRepo.findAll();

        if (userList.isEmpty()){
            throw new NoSearchResultException();
        }

        return userList;
    }

    //Returns specific User
    @GetMapping(path = "/{id}", produces = {"application/json"})
    User findUser(@PathVariable("id") Long id) throws NotFoundException{
        return userRepo.findById(id).orElseThrow(() -> new NotFoundException());
    }

    //Creating user
    @PostMapping(value = "/create", consumes ={"application/json"},produces = {"application/json"})
    String createUser(@RequestBody() User newUser) throws PayloadException {

        if (newUser == null || newUser.getName() == null){
            System.out.println("Test");
            throw new PayloadException();
        }

        userRepo.save(newUser);
        return "User created";
    }

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
