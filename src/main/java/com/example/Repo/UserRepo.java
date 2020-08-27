package com.example.Repo;

import com.example.Entity.User;
import org.springframework.data.repository.CrudRepository;

//CrudRepository for Users
public interface UserRepo extends CrudRepository<User,Long>  {
}

