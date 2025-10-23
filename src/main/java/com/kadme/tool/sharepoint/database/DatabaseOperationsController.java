// Source fixed from decompiled code
package com.kadme.tool.sharepoint.database;

import com.kadme.tool.sharepoint.entity.SnapshotDatabaseBase;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class DatabaseOperationsController<T extends SnapshotDatabaseBase>
        implements CrudBaseRepository<T>, CrudBaseGenericRepository {

    private final CrudBaseRepository<T> genericRepository;
    private final DatabaseOperationsControllerBean databaseOperationsControllerBean;

    public DatabaseOperationsController(CrudBaseRepository<T> genericRepository,
                                        DatabaseOperationsControllerBean databaseOperationsControllerBean) {
        this.genericRepository = genericRepository;
        this.databaseOperationsControllerBean = databaseOperationsControllerBean;
    }

    public CrudBaseGenericRepository getGenericRepository() {
        return (CrudBaseGenericRepository) this.genericRepository;
    }

    @Override
    public <S extends T> S save(S entity) {
        // FIX: no castear a SnapshotDatabaseBase; devolver S
        return this.genericRepository.save(entity);
    }

    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        return this.genericRepository.saveAll(entities);
    }

    @Override
    public Optional<T> findById(Long id) {
        return this.genericRepository.findById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return this.genericRepository.existsById(id);
    }

    @Override
    public List<T> findAll() {
        return this.genericRepository.findAll();
    }

    @Override
    public List<T> findAllById(Iterable<Long> ids) {
        return this.genericRepository.findAllById(ids);
    }

    @Override
    public long count() {
        return this.genericRepository.count();
    }

    @Override
    public void deleteById(Long id) {
        this.genericRepository.deleteById(id);
    }

    @Override
    public void delete(T entity) {
        this.genericRepository.delete(entity);
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        this.genericRepository.deleteAll(entities);
    }

    @Override
    public void deleteAll() {
        this.genericRepository.deleteAll();
    }

    @Override
    public List<T> findAll(Sort sort) {
        return this.genericRepository.findAll(sort);
    }

    @Override
    public void flush() {
        this.genericRepository.flush();
    }

    @Override
    public <S extends T> S saveAndFlush(S entity) {
        // FIX: no castear; devolver S
        return this.genericRepository.saveAndFlush(entity);
    }

    @Override
    public void deleteInBatch(Iterable<T> entities) {
        this.genericRepository.deleteInBatch(entities);
    }

    @Override
    public void deleteAllInBatch() {
        this.genericRepository.deleteAllInBatch();
    }

    @Override
    public T getOne(Long id) {
        // FIX: no castear; devolver T
        return this.genericRepository.getOne(id);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example) {
        return this.genericRepository.findAll(example);
    }

    @Override
    public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
        return this.genericRepository.findAll(example, sort);
    }

    @Override
    public Page<T> findAll(Pageable pageable) {
        return this.genericRepository.findAll(pageable);
    }

    @Override
    public <S extends T> Optional<S> findOne(Example<S> example) {
        return this.genericRepository.findOne(example);
    }

    @Override
    public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
        return this.genericRepository.findAll(example, pageable);
    }

    @Override
    public <S extends T> long count(Example<S> example) {
        // FIX: usar la sobrecarga correcta
        return this.genericRepository.count(example);
    }

    @Override
    public <S extends T> boolean exists(Example<S> example) {
        return this.genericRepository.exists(example);
    }
}
