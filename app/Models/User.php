<?php

namespace App\Models;

use Database\Factories\UserFactory;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Foundation\Auth\User as Authenticatable;
use Illuminate\Notifications\Notifiable;

class User extends Authenticatable
{
    /** @use HasFactory<UserFactory> */
    use HasFactory, Notifiable;

    protected $table = 'User';
    protected $primaryKey = 'iduser';
    public $timestamps = false;
    protected $fillable = [
        'nom',
        'prenom',
        'email',
        'password',
        'idrole',
        'idpays',
        'ville',
        'cnib',
        'latitude',
        'longitude',
        'vendeurInternational',
        'vendeurContratVenteAccepte',
        'compteActif',
        'session_version',
    ];
    protected $hidden = ['password', 'remember_token'];

    protected function casts(): array
    {
        return [
            'vendeurInternational' => 'boolean',
            'vendeurContratVenteAccepte' => 'boolean',
            'compteActif' => 'boolean',
            'password' => 'hashed',
        ];
    }
}
