package com.example.demo.service;


import com.example.demo.model.TodoEntity;
import com.example.demo.persistence.TodoRepository;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class TodoService {

    @Autowired
    private TodoRepository repository;
    public String testService(){
        //TodoEntity 생성
        TodoEntity entity = TodoEntity.builder().title("My first todo item").build();
        //TodoEntity 저장
        repository.save(entity);
        //TodoEntity 검색
        TodoEntity saveEntity = repository.findById(entity.getId()).get();
        return saveEntity.getTitle();

    }

    //Todo아이템을 생성
    public List<TodoEntity> create(final TodoEntity entity){
         //Validations (검증 - 넘오온 엔티티가 유효한지 검사하는 로직
        validate(entity);

        repository.save(entity);   //엔티티를 데이터베이스에 저장하고 로그를 남김

        log.info("Entity Id: {} is saved." , entity.getId());

        return repository.findByUserId(entity.getUserId());     //저장딘 엔티티를 포함하는 새 리스트를 리턴함

    }


    //리팩토링한 메서드 - 검증 부분은 다른 메서드에서도 계속 쓰일 예정!
    private void validate(final TodoEntity entity){
        if(entity == null) {
            log.warn("Entity cnnot be null. ");
            throw new RuntimeException("Entity cnnot be null. ");
        }

        if(entity.getUserId() == null ){
            log.warn("Unknown user. ");
            throw new RuntimeException("Unknown user.");
        }
    }

    //Todo리스트를 검새
    public List<TodoEntity> retrieve(final String userId){
        return repository.findByUserId(userId);
    }

    //Todo를 업데이트
    public List<TodoEntity> update(final TodoEntity entity){
        //(1) 저장할 엔티티가 유효한지 확인한다.
        validate(entity);

        //(2) 넘겨받은 엔티티 id를 이용해 TodoEntity를 가져온다. 존재하지 않는 엔티티는 엡데이트할 수 없기 때문이다.
        final Optional<TodoEntity> original = repository.findById(entity.getId());

        if(original.isPresent()){
            //(3) 반환된 TodoEntity가 존재하면 값을 새 entity 값으로 덮어 씌운다.
            final TodoEntity todo = original.get();
            todo.setTitle(entity.getTitle());
            todo.setDone(entity.isDone());

            //(4) 데이터베이스에 새 값을 저장한다.
            repository.save(todo);
        }
        //Retrieve Todo에서 만든 메서드를 이용해 사용자의 모든 Todo리스트를 리턴한다.
        return retrieve(entity.getUserId());

    }

    //Todo를 삭제
    public List<TodoEntity> delete(final TodoEntity entity){
    //(1)저장할 엔티티가 유효한지 확인한다.
    validate(entity);

    try{
    //(2) 엔티티를 삭제한다.
    repository.delete(entity);
    } catch (Exception e){
    //(3) exception 발생 시 id와 exception을 로깅한다.
    log.error("error deleting entity", entity.getId(), e);
    //(4) 컨트롤러로 exception을 보낸다. 데이터베이스 내부 로직을 캡슐화하려면 e를 리턴하지 않고 새 exception 오브젝트를 리턴한다.
    throw new RuntimeException("error deleting entity" + entity.getId());
    }
    //(5) 새 Todo리스트를 가져와 리턴한다.
    return retrieve(entity.getUserId());
    }


}
