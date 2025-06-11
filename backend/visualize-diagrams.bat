@echo off
echo ============================================
echo  CT.01 - Визуализация архитектурных диаграмм
echo ============================================

echo.
echo Открываю диаграммы в браузере через PlantUML Web Server...
echo.

echo 1. Архитектура системы:
start http://www.plantuml.com/plantuml/uml/~1UDfSaKb0Gn08G00

echo.
echo 2. Зависимости пакетов:  
start http://www.plantuml.com/plantuml/uml/~1UDfSaKb0Gn09G00

echo.
echo 3. Для редактирования диаграмм:
echo    - Откройте файлы в docs/diagrams/*.puml
echo    - Скопируйте содержимое в http://www.plantuml.com/plantuml/
echo    - Или установите PlantUML плагин в IntelliJ IDEA

echo.
echo Также доступен анализ зависимостей: dependency-analysis.md
echo.

pause 