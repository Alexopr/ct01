import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { TelegramAuthModal } from "../components/auth";
import { createUser } from "../services/userService";
import { Button, Input, Card, Alert } from "../components/ui";
import { Link } from "@heroui/react";

const Register: React.FC = () => {
  const [form, setForm] = useState({ username: "", password: "", email: "" });
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [tgOpen, setTgOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  React.useEffect(() => {
    if (success) {
      const t = setTimeout(() => navigate('/login'), 1200);
      return () => clearTimeout(t);
    }
  }, [success, navigate]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(false);
    setLoading(true);
    
    try {
      await createUser({ ...form, roles: ["USER"] });
      setSuccess(true);
      setForm({ username: "", password: "", email: "" });
    } catch (e: any) {
      setError(e.message || "Ошибка регистрации");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/90 to-background/80 flex items-center justify-center p-4">
      <div className="w-full max-w-md animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        <Card
          variant="glass"
          className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-2xl"
          padding="lg"
        >
          <div className="text-center space-y-6">
            {/* Header */}
            <div className="space-y-2">
              <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                Регистрация
              </h1>
              <p className="text-foreground-600 text-sm">
                Создайте новый аккаунт для начала работы
              </p>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              <Input
                label="Имя пользователя"
                name="username"
                value={form.username}
                onChange={handleChange}
                variant="bordered"
                fullWidth
                required
                autoFocus
                leftIcon="solar:user-bold"
                glassmorphism
                placeholder="Введите имя пользователя"
              />
              
              <Input
                label="Email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                variant="bordered"
                fullWidth
                required
                leftIcon="solar:letter-bold"
                glassmorphism
                placeholder="Введите email"
              />
              
              <Input
                label="Пароль"
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                variant="bordered"
                fullWidth
                required
                leftIcon="solar:lock-password-bold"
                glassmorphism
                placeholder="Введите пароль"
              />

              <Button
                type="submit"
                variant="primary"
                size="lg"
                fullWidth
                gradient
                disabled={loading}
                className="transition-all duration-300 hover:shadow-xl hover:shadow-primary/25"
              >
                {loading ? "Регистрация..." : "Зарегистрироваться"}
              </Button>

              <Button
                variant="ghost"
                size="lg"
                fullWidth
                onClick={() => setTgOpen(true)}
                icon="logos:telegram"
                className="border border-divider/30 hover:border-primary/50 transition-all duration-300"
                disabled={loading}
              >
                Зарегистрироваться через Telegram
              </Button>

              {error && (
                <div className="animate-in fade-in-0 slide-in-from-top-2 duration-300">
                  <Alert
                    type="error"
                    title="Ошибка регистрации"
                    description={error}
                    variant="glass"
                  />
                </div>
              )}
              
              {success && (
                <div className="animate-in fade-in-0 slide-in-from-top-2 duration-300">
                  <Alert
                    type="success"
                    title="Регистрация успешна!"
                    description="Переходим на страницу входа..."
                    variant="glass"
                  />
                </div>
              )}
            </form>

            {/* Footer */}
            <div className="pt-4 border-t border-divider/20">
              <p className="text-sm text-foreground-600">
                Уже есть аккаунт?{" "}
                <Link
                  href="/login"
                  className="text-primary hover:text-primary-400 transition-colors duration-200 font-medium"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate('/login');
                  }}
                >
                  Войти
                </Link>
              </p>
            </div>
          </div>
        </Card>

        <TelegramAuthModal open={tgOpen} onClose={() => setTgOpen(false)} />
      </div>
    </div>
  );
};

export default Register; 