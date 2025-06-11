package com.ct01.core.application;

/**
 * Маркерный интерфейс для всех Use Cases в системе.
 * Use Case представляет отдельный сценарий использования системы,
 * инкапсулируя бизнес-логику конкретной операции.
 * 
 * Принципы Use Case:
 * - Один Use Case = одна бизнес-операция
 * - Оркестрация доменных объектов
 * - Транзакционная граница
 * - Не содержит бизнес-правил (они в домене)
 * - Координирует работу между агрегатами
 */
public interface UseCase<TRequest, TResponse> {
    
    /**
     * Выполнить сценарий использования
     * @param request запрос с данными для выполнения
     * @return ответ с результатом выполнения
     */
    TResponse execute(TRequest request);
}

/**
 * Интерфейс для Use Cases без входных параметров
 */
@FunctionalInterface
interface VoidInputUseCase<TResponse> {
    TResponse execute();
}

/**
 * Интерфейс для Use Cases без выходных параметров
 */
@FunctionalInterface
interface VoidOutputUseCase<TRequest> {
    void execute(TRequest request);
}

/**
 * Интерфейс для Use Cases без параметров
 */
@FunctionalInterface
interface VoidUseCase {
    void execute();
} 
