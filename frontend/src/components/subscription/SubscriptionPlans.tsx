import React, { useEffect, useState } from 'react';
import { 
  Card, 
  CardBody, 
  CardHeader, 
  Button, 
  Chip, 
  Divider,
  Spinner,
  Table,
  TableBody,
  TableCell,
  TableColumn,
  TableHeader,
  TableRow
} from '@nextui-org/react';
import { Icon } from '@iconify/react';
import { subscriptionApi } from '../../services/api';
import type { SubscriptionPlan, SubscriptionFeatureMatrix } from '../../types/api';

interface SubscriptionPlansProps {
  onPlanSelect?: (plan: SubscriptionPlan) => void;
  currentPlan?: string;
}

export const SubscriptionPlans: React.FC<SubscriptionPlansProps> = ({ 
  onPlanSelect,
  currentPlan 
}) => {
  const [plans, setPlans] = useState<SubscriptionPlan[]>([]);
  const [featureMatrix, setFeatureMatrix] = useState<SubscriptionFeatureMatrix | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const [plansResponse, matrixResponse] = await Promise.all([
          subscriptionApi.getPlans(),
          subscriptionApi.getFeatureMatrix()
        ]);
        setPlans(plansResponse);
        setFeatureMatrix(matrixResponse);
      } catch (err) {
        setError('Ошибка загрузки тарифных планов');
        console.error('Error fetching subscription plans:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const formatPrice = (price: number, currency: string, billingCycle: string) => {
    if (price === 0) return 'Бесплатно';
    const period = billingCycle === 'MONTHLY' ? 'мес' : 'год';
    return `${price} ${currency}/${period}`;
  };

  const renderFeatureValue = (value: string | number | boolean) => {
    if (typeof value === 'boolean') {
      return value ? (
        <Icon icon="material-symbols:check" className="h-5 w-5 text-success" />
      ) : (
        <Icon icon="material-symbols:close" className="h-5 w-5 text-danger" />
      );
    }
    
    if (typeof value === 'number') {
      return value === -1 ? '∞' : value.toString();
    }
    
    return value.toString();
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center p-8">
        <Spinner size="lg" />
      </div>
    );
  }

  if (error) {
    return (
      <div className="text-center p-8">
        <p className="text-danger">{error}</p>
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Plans Comparison Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {plans.map((plan) => {
          const isCurrentPlan = currentPlan === plan.id;
          const isRecommended = plan.isRecommended;
          
          return (
            <Card 
              key={plan.id} 
              className={`relative ${isCurrentPlan ? 'ring-2 ring-primary' : ''} ${isRecommended ? 'ring-2 ring-warning' : ''}`}
            >
              {isRecommended && (
                <div className="absolute -top-3 left-1/2 transform -translate-x-1/2">
                  <Chip color="warning" size="sm">
                    Рекомендуется
                  </Chip>
                </div>
              )}
              
              {isCurrentPlan && (
                <div className="absolute -top-3 right-4">
                  <Chip color="primary" size="sm">
                    Текущий план
                  </Chip>
                </div>
              )}

              <CardHeader className="flex flex-col items-start gap-2 pb-2">
                <h3 className="text-xl font-bold">{plan.displayName}</h3>
                <p className="text-default-600 text-sm">{plan.description}</p>
                <div className="text-2xl font-bold text-primary">
                  {formatPrice(plan.price, plan.currency, plan.billingCycle)}
                </div>
                {plan.trialDays && plan.trialDays > 0 && (
                  <Chip size="sm" color="success" variant="flat">
                    {plan.trialDays} дней бесплатно
                  </Chip>
                )}
              </CardHeader>

              <Divider />

              <CardBody className="space-y-4">
                <div className="space-y-2">
                  <h4 className="font-semibold text-sm">Возможности:</h4>
                  <ul className="space-y-1">
                    {plan.features.map((feature, index) => (
                      <li key={index} className="flex items-center gap-2 text-sm">
                        <Icon icon="material-symbols:check" className="h-4 w-4 text-success flex-shrink-0" />
                        <span>{feature}</span>
                      </li>
                    ))}
                  </ul>
                </div>

                <Button
                  color={isCurrentPlan ? "default" : "primary"}
                  variant={isCurrentPlan ? "bordered" : "solid"}
                  onPress={() => !isCurrentPlan && onPlanSelect?.(plan)}
                  disabled={isCurrentPlan}
                  className="w-full"
                >
                  {isCurrentPlan ? 'Активный план' : 'Выбрать план'}
                </Button>
              </CardBody>
            </Card>
          );
        })}
      </div>

      {/* Feature Comparison Matrix */}
      {featureMatrix && (
        <div className="space-y-4">
          <h3 className="text-xl font-bold">Сравнение возможностей</h3>
          <Card>
            <CardBody>
              <Table aria-label="Сравнение тарифных планов">
                <TableHeader>
                  <TableColumn>Функция</TableColumn>
                  <TableColumn>FREE</TableColumn>
                  <TableColumn>PREMIUM</TableColumn>
                </TableHeader>
                <TableBody>
                  {Object.entries(featureMatrix).map(([feature, values]) => (
                    <TableRow key={feature}>
                      <TableCell className="font-medium">{feature}</TableCell>
                      <TableCell>
                        <div className="flex justify-center">
                          {renderFeatureValue(values.free)}
                        </div>
                      </TableCell>
                      <TableCell>
                        <div className="flex justify-center">
                          {renderFeatureValue(values.premium)}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardBody>
          </Card>
        </div>
      )}
    </div>
  );
}; 