<?php

namespace App\Services;

use App\Models\PasswordResetToken;
use App\Models\User;
use Illuminate\Support\Facades\Hash;

class PasswordResetService
{
    public function createTokenForEmail(string $email): ?string
    {
        $user = User::where('email', $email)->first();
        if (! $user) {
            return null;
        }

        $raw = bin2hex(random_bytes(24));
        PasswordResetToken::create([
            'iduser' => $user->iduser,
            'tokenHash' => hash('sha256', $raw),
            'expiresAt' => now()->addMinutes(30),
            'used' => false,
            'datecreation' => now(),
        ]);

        return $raw;
    }

    public function reset(string $token, string $newPassword): bool
    {
        $hashed = hash('sha256', $token);
        $row = PasswordResetToken::where('tokenHash', $hashed)
            ->where('used', false)
            ->where('expiresAt', '>', now())
            ->orderByDesc('id')
            ->first();

        if (! $row) {
            return false;
        }

        $user = User::find($row->iduser);
        if (! $user) {
            return false;
        }

        $user->password = Hash::make($newPassword);
        $user->session_version = ((int) ($user->session_version ?? 0)) + 1;
        $user->save();

        $row->used = true;
        $row->usedAt = now();
        $row->save();

        return true;
    }
}
