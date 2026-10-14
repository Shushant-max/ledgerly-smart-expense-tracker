package com.ledgerly;
import org.springframework.data.jpa.repository.JpaRepository;import java.util.*;
interface UserRepository extends JpaRepository<UserEntity,Long>{Optional<UserEntity> findByEmailIgnoreCase(String email);boolean existsByEmailIgnoreCase(String email);}
interface TransactionRepository extends JpaRepository<TransactionEntity,Long>{List<TransactionEntity> findByUserIdOrderByDateDescIdDesc(Long userId);Optional<TransactionEntity> findByIdAndUserId(Long id,Long userId);}
interface BudgetRepository extends JpaRepository<BudgetEntity,Long>{Optional<BudgetEntity> findByUserIdAndMonthKey(Long userId,String monthKey);}
