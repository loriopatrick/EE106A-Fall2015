function y = decround(x,n)
% x‚Ì¬”“_ˆÈ‰ºnŒ…‚ğÅ‚à‹ß‚¢(n-1)Œ…‚Ì¬”‚ÉŠÛ‚ß‚é

unit = 10^(n-1);
y = (round(x * unit)) / unit;