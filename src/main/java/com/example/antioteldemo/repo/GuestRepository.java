package com.example.antioteldemo.repo;

import com.example.antioteldemo.entities.Guest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestRepository extends CrudRepository<Guest, Long> {

}
