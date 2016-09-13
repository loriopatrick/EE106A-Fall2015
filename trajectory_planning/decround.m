function y = decround(x,n)
% xの小数点以下n桁を最も近い(n-1)桁の小数に丸める

unit = 10^(n-1);
y = (round(x * unit)) / unit;