package com.example.Repo;

import com.example.Entity.HolidayRequest;
import org.springframework.data.repository.CrudRepository;

//CrudRepository for HolidayRequests
public interface HolidayRequestRepo extends CrudRepository<HolidayRequest,Long>{
}
