<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class Article extends Model
{
    protected $table = 'Article';
    protected $primaryKey = 'idarticle';
    public $timestamps = false;
    protected $guarded = [];
}
