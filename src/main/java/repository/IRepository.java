package repository;

import java.util.List;

public interface IRepository<T> {
    void saveAll(List<T> items);
    List<T> loadAll();
}