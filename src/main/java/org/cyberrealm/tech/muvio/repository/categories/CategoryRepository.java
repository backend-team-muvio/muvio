package org.cyberrealm.tech.muvio.repository.categories;

import org.cyberrealm.tech.muvio.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, Category.About> {
}
