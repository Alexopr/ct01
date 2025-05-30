# HeroUI Component Library

Библиотека переиспользуемых UI компонентов для криптоприложения на базе HeroUI и Tailwind CSS.

## Установка и импорт

```tsx
// Импорт всех компонентов
import { Button, Card, Input, Alert, Modal } from '@/components/ui';

// Или импорт отдельных компонентов
import { Button } from '@/components/ui/Button';
```

## Компоненты

### Button

Кнопка с поддержкой различных стилей, размеров и иконок.

```tsx
import { Button } from '@/components/ui';

// Основные варианты
<Button variant="primary">Основная кнопка</Button>
<Button variant="secondary">Вторичная кнопка</Button>
<Button variant="ghost">Прозрачная кнопка</Button>
<Button variant="danger">Опасное действие</Button>

// С иконками
<Button icon="solar:plus-bold" iconPosition="left">Добавить</Button>
<Button icon="solar:arrow-right-bold" iconPosition="right">Далее</Button>

// С градиентом
<Button variant="primary" gradient>Градиентная кнопка</Button>

// Во всю ширину
<Button fullWidth>Полная ширина</Button>
```

**Props:**
- `variant`: 'primary' | 'secondary' | 'ghost' | 'danger' | 'success' | 'warning'
- `size`: 'sm' | 'md' | 'lg'
- `icon`: строка с именем иконки Iconify
- `iconPosition`: 'left' | 'right'
- `fullWidth`: boolean
- `gradient`: boolean (только для variant="primary")

### Card

Карточка с поддержкой glassmorphism эффектов и различных стилей.

```tsx
import { Card } from '@/components/ui';

// Основные варианты
<Card variant="default">Обычная карточка</Card>
<Card variant="glass">Стеклянный эффект</Card>
<Card variant="gradient">Градиентная карточка</Card>
<Card variant="bordered">С рамкой</Card>
<Card variant="elevated">Приподнятая</Card>

// С заголовком и подвалом
<Card 
  header={<h3>Заголовок</h3>}
  footer={<Button>Действие</Button>}
>
  Содержимое карточки
</Card>

// Интерактивная карточка
<Card hoverable clickable onClick={() => console.log('Click')}>
  Кликабельная карточка
</Card>
```

**Props:**
- `variant`: 'default' | 'glass' | 'gradient' | 'bordered' | 'elevated'
- `size`: 'sm' | 'md' | 'lg'
- `header`: React.ReactNode
- `footer`: React.ReactNode
- `padding`: 'none' | 'sm' | 'md' | 'lg'
- `hoverable`: boolean
- `clickable`: boolean
- `blur`: boolean

### Input

Поле ввода с поддержкой иконок, валидации и glassmorphism эффектов.

```tsx
import { Input } from '@/components/ui';

// Основные варианты
<Input placeholder="Обычное поле" />
<Input variant="bordered" placeholder="С рамкой" />
<Input variant="underlined" placeholder="Подчеркнутое" />

// С иконками
<Input 
  leftIcon="solar:user-bold" 
  placeholder="Имя пользователя" 
/>
<Input 
  rightIcon="solar:eye-bold" 
  type="password"
  placeholder="Пароль"
  onRightIconClick={() => setShowPassword(!showPassword)}
/>

// Состояния валидации
<Input state="error" placeholder="Ошибка" />
<Input state="success" placeholder="Успех" />
<Input state="warning" placeholder="Предупреждение" />

// С glassmorphism эффектом
<Input glassmorphism placeholder="Стеклянный эффект" />
```

**Props:**
- `variant`: 'default' | 'bordered' | 'flat' | 'faded' | 'underlined'
- `state`: 'default' | 'error' | 'success' | 'warning'
- `leftIcon`: строка с именем иконки
- `rightIcon`: строка с именем иконки
- `onRightIconClick`: функция клика по правой иконке
- `fullWidth`: boolean
- `glassmorphism`: boolean

### Alert

Компонент для отображения уведомлений и сообщений.

```tsx
import { Alert } from '@/components/ui';

// Типы уведомлений
<Alert type="info" title="Информация" description="Это информационное сообщение" />
<Alert type="success" title="Успех" description="Операция выполнена успешно" />
<Alert type="warning" title="Предупреждение" description="Обратите внимание на это" />
<Alert type="error" title="Ошибка" description="Что-то пошло не так" />

// Варианты стиля
<Alert type="info" variant="filled" title="Заливка" />
<Alert type="info" variant="bordered" title="С рамкой" />
<Alert type="info" variant="flat" title="Плоский" />
<Alert type="info" variant="glass" title="Стеклянный" />

// Закрываемое уведомление
<Alert 
  type="success" 
  title="Успех" 
  closable 
  onClose={() => setAlertVisible(false)}
/>

// Кастомная иконка
<Alert icon="solar:star-bold" title="Кастомная иконка" />
```

**Props:**
- `type`: 'info' | 'success' | 'warning' | 'error'
- `title`: строка заголовка
- `description`: строка описания
- `variant`: 'filled' | 'bordered' | 'flat' | 'glass'
- `closable`: boolean
- `onClose`: функция закрытия
- `icon`: string | boolean (кастомная иконка или показать/скрыть)

### Modal

Модальное окно с поддержкой различных размеров и эффектов.

```tsx
import { Modal, useDisclosure } from '@/components/ui';

const { isOpen, onOpen, onClose } = useDisclosure();

// Основное использование
<Modal 
  isOpen={isOpen} 
  onClose={onClose}
  title="Заголовок модального окна"
  description="Описание модального окна"
>
  Содержимое модального окна
</Modal>

// Различные размеры
<Modal size="sm" title="Маленькое окно">...</Modal>
<Modal size="lg" title="Большое окно">...</Modal>
<Modal size="full" title="На весь экран">...</Modal>

// Варианты стиля
<Modal variant="glass" title="Стеклянный эффект">...</Modal>
<Modal variant="gradient" title="Градиентное окно">...</Modal>

// С кастомным подвалом
<Modal 
  title="Подтверждение"
  footer={
    <div className="flex gap-2">
      <Button variant="ghost" onClick={onClose}>Отмена</Button>
      <Button variant="danger" onClick={handleDelete}>Удалить</Button>
    </div>
  }
>
  Вы уверены, что хотите удалить этот элемент?
</Modal>
```

**Props:**
- `size`: 'xs' | 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full'
- `title`: React.ReactNode
- `description`: строка
- `footer`: React.ReactNode
- `variant`: 'default' | 'glass' | 'gradient'
- `closeButton`: boolean
- `blur`: boolean
- `scrollBehavior`: 'inside' | 'outside'

## Стилизация и темы

Все компоненты поддерживают:
- **Темную тему** из коробки
- **Glassmorphism эффекты** (прозрачность + размытие)
- **Градиенты** для современного внешнего вида
- **Анимации** при наведении и взаимодействии
- **Адаптивность** на всех устройствах

## Лучшие практики

1. **Консистентность**: Используйте одинаковые variants и sizes в рамках одного интерфейса
2. **Доступность**: Все компоненты поддерживают ARIA атрибуты
3. **Производительность**: Компоненты оптимизированы и поддерживают tree-shaking
4. **Кастомизация**: Используйте className для дополнительной стилизации

## Примеры использования

### Форма входа
```tsx
<Card variant="glass" className="max-w-md mx-auto">
  <div className="space-y-4">
    <Input 
      leftIcon="solar:user-bold"
      placeholder="Email"
      type="email"
      fullWidth
    />
    <Input 
      leftIcon="solar:lock-password-bold"
      rightIcon="solar:eye-bold"
      placeholder="Пароль"
      type="password"
      fullWidth
    />
    <Button variant="primary" gradient fullWidth>
      Войти
    </Button>
  </div>
</Card>
```

### Информационная панель
```tsx
<div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
  <Card variant="gradient" hoverable>
    <div className="text-center">
      <h3 className="text-2xl font-bold">$12,345</h3>
      <p className="text-foreground-600">Общий баланс</p>
    </div>
  </Card>
  
  <Card variant="glass" hoverable>
    <div className="text-center">
      <h3 className="text-2xl font-bold text-success">+15.6%</h3>
      <p className="text-foreground-600">Рост за день</p>
    </div>
  </Card>
</div>
```

---

Для получения дополнительной информации обращайтесь к документации HeroUI: https://heroui.com/ 