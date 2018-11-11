package com.codeontheweb;

import org.springframework.data.repository.CrudRepository;

public interface RandomDataRepository extends CrudRepository<RandomData, Long>
{
	//Extending CrudRepository provides a bunch of CRUD functionality automatically.
}
