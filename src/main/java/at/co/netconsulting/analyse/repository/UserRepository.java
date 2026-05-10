package at.co.netconsulting.analyse.repository;

import at.co.netconsulting.analyse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    java.util.List<User> findAllByOrderByFirstnameAsc();
}
