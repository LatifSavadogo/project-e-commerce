<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Wallet extends Model
{
    protected $table = 'Wallet';
    protected $primaryKey = 'idwallet';
    public $timestamps = false;
    protected $guarded = [];
}
