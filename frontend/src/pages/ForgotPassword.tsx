import React, { useState } from "react";
import { Icon } from '@iconify/react';
import { useNavigate } from 'react-router-dom';
import { Button, Input, Card, Alert } from "../components/ui";
import { Link } from "@nextui-org/react";

const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setMessage(null);

    try {
      // TODO: Implement forgot password API call
      // await api.post('/auth/forgot-password', { email });
      
      // Временная имитация для демонстрации
      await new Promise(resolve => setTimeout(resolve, 1000));
      setMessage("Инструкции по восстановлению пароля отправлены на вашу почту");
      
    } catch (err: any) {
      setError(err.response?.data?.message || "Ошибка при отправке инструкций");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-background/90 to-background/80 flex items-center justify-center p-4">
      <div className="w-full max-w-md animate-in fade-in-0 slide-in-from-bottom-4 duration-1000">
        <Card
          
          className="backdrop-blur-xl bg-background/30 border border-divider/20 shadow-2xl"
        >
          <div className="text-center space-y-6">
            {/* Header */}
            <div className="space-y-2">
              <h1 className="text-3xl font-bold bg-gradient-to-r from-primary to-secondary bg-clip-text text-transparent">
                Восстановление пароля
              </h1>
              <p className="text-foreground-600 text-sm">
                Введите ваш email для получения инструкций по восстановлению пароля
              </p>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} className="space-y-4">
              <Input
                label="Email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                variant="bordered"
                fullWidth
                required
                autoFocus
                startContent={<Icon icon="solar:letter-bold" className="w-4 h-4" />}
                placeholder="Введите ваш email"
              />

              <Button
                type="submit"
                color="primary"
                size="lg"
                fullWidth disabled={loading}
                className="transition-all duration-300 hover:shadow-xl hover:shadow-primary/25"
              >
                {loading ? 'Отправка...' : 'Отправить инструкции'}
              </Button>

              <Button
                variant="ghost"
                size="lg"
                fullWidth
                onClick={() => navigate('/login')}
                className="border border-divider/30 hover:border-primary/50 transition-all duration-300"
                disabled={loading}
              >
                Вернуться к входу
              </Button>

              {error && (
                <div className="animate-in fade-in-0 slide-in-from-top-2 duration-300">
                  <Alert
                    type="error"
                    title="Ошибка"
                    description={error}
                    
                  />
                </div>
              )}
              
              {message && (
                <div className="animate-in fade-in-0 slide-in-from-top-2 duration-300">
                  <Alert
                    type="success"
                    title="Инструкции отправлены"
                    description={message}
                    
                  />
                </div>
              )}
            </form>

            {/* Footer */}
            <div className="pt-4 border-t border-divider/20">
              <p className="text-sm text-foreground-600">
                Вспомнили пароль?{" "}
                <Link
                  href="/login"
                  className="text-primary hover:text-primary-400 transition-colors duration-200 font-medium"
                  onClick={(e) => {
                    e.preventDefault();
                    navigate('/login');
                  }}
                >
                  Войти в аккаунт
                </Link>
              </p>
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default ForgotPassword; 




