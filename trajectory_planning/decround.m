function y = decround(x,n)
% x�̏����_�ȉ�n�����ł��߂�(n-1)���̏����Ɋۂ߂�

unit = 10^(n-1);
y = (round(x * unit)) / unit;