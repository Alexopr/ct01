import React, { useEffect, useState } from 'react';
import { Card, CardBody, CardHeader, Divider, Button, useDisclosure } from '@nextui-org/react';
import { Icon } from '@iconify/react';
import { SubscriptionStatus } from './SubscriptionStatus';
import { SubscriptionPlans } from './SubscriptionPlans';
import { LimitIndicator } from './LimitIndicator';
import { UpgradeModal } from './UpgradeModal';
import { subscriptionApi } from '../../services/api';
import type { SubscriptionPlan, SubscriptionStatusDto, UsageLimitDto } from '../../types/api';

export const SubscriptionDashboard: React.FC = () => {
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [currentStatus, setCurrentStatus] = useState<SubscriptionStatusDto | null>(null);
  const [limits, setLimits] = useState<UsageLimitDto[]>([]);
  const [loading, setLoading] = useState(true);
  const { isOpen, onOpen, onClose } = useDisclosure();

  const fetchData = async () => {
    try {
      setLoading(true);
      const [plansResponse, statusResponse, limitsResponse] = await Promise.all([
        subscriptionApi.getPlans(),
        subscriptionApi.getStatus(),
        subscriptionApi.getLimits()
      ]);
      setPlans(plansResponse);
      setCurrentStatus(statusResponse);
      setLimits(limitsResponse);
    } catch (err) {
      console.error('Error fetching subscription data:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleUpgradeSuccess = () => {
    fetchData(); // Reload data after successful upgrade
  };

  const handlePlanSelect = () => {
    onOpen();
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="text-center">
          <h1 className="text-2xl font-bold mb-2">Управление подпиской</h1>
          <p className="text-default-600">Загрузка...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto p-6 space-y-8">
      {/* Header */}
      <div className="text-center space-y-2">
        <h1 className="text-3xl font-bold">Управление подпиской</h1>
        <p className="text-default-600">
          Управляйте своим тарифным планом и отслеживайте использование ресурсов
        </p>
      </div>

      {/* Current Status and Limits */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Subscription Status */}
        <div className="lg:col-span-2">
          <SubscriptionStatus onUpgradeClick={onOpen} />
        </div>

        {/* Quick Limits Overview */}
        <Card>
          <CardHeader className="flex gap-3">
            <Icon icon="material-symbols:analytics" className="h-6 w-6 text-primary" />
            <div className="flex flex-col">
              <p className="text-md font-semibold">Использование лимитов</p>
              <p className="text-sm text-default-500">Текущий период</p>
            </div>
          </CardHeader>
          <Divider />
          <CardBody className="space-y-4">
            {limits.length > 0 ? (
              limits.slice(0, 4).map((limit, index) => (
                <LimitIndicator 
                  key={index}
                  limit={limit}
                  size="sm"
                  showLabel={true}
                />
              ))
            ) : (
              <p className="text-center text-default-500 text-sm">
                Нет данных об использовании
              </p>
            )}
            
            {limits.length > 4 && (
              <Button
                variant="light"
                size="sm"
                className="w-full"
                startContent={<Icon icon="material-symbols:expand-more" />}
              >
                Показать все ({limits.length})
              </Button>
            )}
          </CardBody>
        </Card>
      </div>

      {/* Detailed Limits */}
      {limits.length > 0 && (
        <Card>
          <CardHeader>
            <div className="flex items-center gap-3">
              <Icon icon="material-symbols:monitoring" className="h-6 w-6 text-primary" />
              <div>
                <h3 className="text-lg font-semibold">Детальное использование</h3>
                <p className="text-sm text-default-500">
                  Подробная информация по всем лимитам ресурсов
                </p>
              </div>
            </div>
          </CardHeader>
          <Divider />
          <CardBody>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {limits.map((limit, index) => (
                <LimitIndicator 
                  key={index}
                  limit={limit}
                  size="md"
                  showLabel={true}
                />
              ))}
            </div>
          </CardBody>
        </Card>
      )}

      {/* Available Plans */}
      <Card>
        <CardHeader>
          <div className="flex items-center gap-3">
            <Icon icon="material-symbols:workspace-premium" className="h-6 w-6 text-primary" />
            <div>
              <h3 className="text-lg font-semibold">Доступные тарифы</h3>
              <p className="text-sm text-default-500">
                Сравните возможности различных тарифных планов
              </p>
            </div>
          </div>
        </CardHeader>
        <Divider />
        <CardBody>
          <SubscriptionPlans 
            onPlanSelect={handlePlanSelect}
            currentPlan={currentStatus?.planId}
          />
        </CardBody>
      </Card>

      {/* Upgrade Modal */}
      <UpgradeModal
        isOpen={isOpen}
        onClose={onClose}
        plans={plans}
        currentPlan={currentStatus?.planId}
        onUpgradeSuccess={handleUpgradeSuccess}
      />
    </div>
  );
}; 