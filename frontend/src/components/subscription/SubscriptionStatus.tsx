import React, { useEffect, useState } from 'react';
import { Card, CardBody, CardHeader, Chip, Progress, Button, Spinner } from '@nextui-org/react';
import { subscriptionApi } from '../../services/api';
import type { SubscriptionStatusDto, UsageLimitDto } from '../../types/api';

interface SubscriptionStatusProps {
  onUpgradeClick?: () => void;
}

export const SubscriptionStatus: React.FC<SubscriptionStatusProps> = ({ onUpgradeClick }) => {
  const [status, setStatus] = useState<SubscriptionStatusDto | null>(null);
  const [limits, setLimits] = useState<UsageLimitDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [statusResponse, limitsResponse] = await Promise.all([
          subscriptionApi.getStatus(),
          subscriptionApi.getLimits()
        ]);
        setStatus(statusResponse);
        setLimits(limitsResponse);
      } catch (err) {
        setError('Ошибка загрузки данных подписки');
        console.error('Error fetching subscription data:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <Card className="w-full">
        <CardBody className="flex items-center justify-center p-6">
          <Spinner size="lg" />
        </CardBody>
      </Card>
    );
  }

  if (error || !status) {
    return (
      <Card className="w-full">
        <CardBody className="text-center p-6">
          <p className="text-danger">{error || 'Не удалось загрузить данные подписки'}</p>
        </CardBody>
      </Card>
    );
  }

  const getPlanColor = (plan: string) => {
    switch (plan?.toUpperCase()) {
      case 'FREE':
        return 'default';
      case 'PREMIUM':
        return 'primary';
      default:
        return 'secondary';
    }
  };

  const getStatusColor = (active: boolean) => {
    return active ? 'success' : 'danger';
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ru-RU');
  };

  const getUsagePercentage = (used: number, limit: number) => {
    if (limit === -1) return 0; // Unlimited
    return Math.min((used / limit) * 100, 100);
  };

  const getUsageColor = (percentage: number) => {
    if (percentage >= 90) return 'danger';
    if (percentage >= 70) return 'warning';
    return 'primary';
  };

  return (
    <Card className="w-full">
      <CardHeader className="flex gap-3">
        <div className="flex flex-col">
          <p className="text-md font-semibold">Статус подписки</p>
          <div className="flex gap-2 items-center">
            <Chip 
              color={getPlanColor(status.planName)} 
              size="sm"
              variant="flat"
            >
              {status.planName}
            </Chip>
            <Chip 
              color={getStatusColor(status.active)} 
              size="sm"
              variant="dot"
            >
              {status.active ? 'Активна' : 'Неактивна'}
            </Chip>
          </div>
        </div>
      </CardHeader>
      <CardBody className="gap-4">
        {/* Subscription Details */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {status.startDate && (
            <div>
              <p className="text-sm text-default-600">Дата начала</p>
              <p className="font-medium">{formatDate(status.startDate)}</p>
            </div>
          )}
          {status.endDate && (
            <div>
              <p className="text-sm text-default-600">Дата окончания</p>
              <p className="font-medium">{formatDate(status.endDate)}</p>
            </div>
          )}
        </div>

        {/* Usage Limits */}
        {limits.length > 0 && (
          <div className="space-y-3">
            <p className="text-sm font-semibold text-default-700">Использование лимитов</p>
            {limits.map((limit, index) => {
              const percentage = getUsagePercentage(limit.used, limit.limit);
              const isUnlimited = limit.limit === -1;
              
              return (
                <div key={index} className="space-y-2">
                  <div className="flex justify-between items-center">
                    <span className="text-sm font-medium">{limit.resourceType}</span>
                    <span className="text-sm text-default-600">
                      {isUnlimited 
                        ? `${limit.used} / ∞` 
                        : `${limit.used} / ${limit.limit}`
                      }
                    </span>
                  </div>
                  {!isUnlimited && (
                    <Progress 
                      value={percentage}
                      color={getUsageColor(percentage)}
                      size="sm"
                      className="w-full"
                    />
                  )}
                </div>
              );
            })}
          </div>
        )}

        {/* Trial Information */}
        {status.trialActive && status.trialEndDate && (
          <div className="bg-warning-50 border border-warning-200 rounded-lg p-3">
            <p className="text-sm font-medium text-warning-800">
              Пробный период активен до {formatDate(status.trialEndDate)}
            </p>
          </div>
        )}

        {/* Upgrade Button */}
        {status.planName?.toUpperCase() === 'FREE' && onUpgradeClick && (
          <Button 
            color="primary" 
            variant="solid"
            onPress={onUpgradeClick}
            className="w-full mt-2"
          >
            Обновить подписку
          </Button>
        )}
      </CardBody>
    </Card>
  );
}; 